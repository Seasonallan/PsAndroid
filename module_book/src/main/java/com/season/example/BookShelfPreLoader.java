package com.season.example;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.season.book.R;
import com.season.book.bean.BookInfo;
import com.season.book.plugin.epub.EpubPlugin;
import com.season.book.plugin.umd.UmdPlugin;
import com.season.lib.BaseContext;
import com.season.lib.support.bitmap.BitmapUtil;
import com.season.lib.support.file.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 书架书籍预加载
 */
public class BookShelfPreLoader {

    public interface ICallback {
        void onBookLoaded(List<BookInfo> bookLists);
    }

    private static BookShelfPreLoader sInstance = null;
    private List<BookInfo> bookLists;
    private ICallback iCallback;
    private boolean isPreLoaded = false;

    private BookShelfPreLoader() {
    }

    public void getBookLists(ICallback callback) {
        if (isPreLoaded) {
            if (bookLists != null) {
                callback.onBookLoaded(bookLists);
            } else {
                this.iCallback = callback;
                isPreLoaded = false;
                preLoad();
            }
        } else {
            this.iCallback = callback;
        }
    }

    public static final BookShelfPreLoader getInstance() {
        if (sInstance == null) {
            sInstance = new BookShelfPreLoader();
        }
        return sInstance;
    }



    public void saveShelfBooks(final List list) {
        if (list == null || list.size() == 0){
            return;
        }
        bookLists = list;
        new Thread() {
            @Override
            public void run() {
                FileUtils.saveSerialData("cacheBookLists", list, BaseContext.getInstance().getCacheDir());
            }
        }.start();
    }

    public void preLoad() {
        new Thread() {
            @Override
            public void run() {
                Object object = FileUtils.getSerialData("cacheBookLists", BaseContext.getInstance().getCacheDir());
                if (object instanceof List) {
                    bookLists = (List<BookInfo>) object;
                }
               //bookLists = null;
                if (bookLists == null || bookLists.size() == 0) {
                    bookLists = new ArrayList<>();
                    bookLists.add(new BookInfo("00001", "1.epub", R.raw.epub_book));
                    bookLists.add(new BookInfo("00011", "2.epub", R.raw.epub_book2));
//                    bookLists.add(new BookInfo("00012", "3.epub", R.raw.santi));
//                    bookLists.add(new BookInfo("00013", "4.epub", R.raw.zuoer));
//                    bookLists.add(new BookInfo("000123", "凡人修仙传.txt", R.raw.frxxz));
//                    bookLists.add(new BookInfo("00014", "求魔.txt", R.raw.qiumo));
//                    bookLists.add(new BookInfo("000144", "明骑.txt", R.raw.mingqi));
//                    bookLists.add(new BookInfo("000114", "大明.txt", R.raw.daming));
//                    bookLists.add(new BookInfo("00014", "5.epub", R.raw.ssssslth));
//                    bookLists.add(new BookInfo("00015", "原始战记.txt", R.raw.yszj));
//                    bookLists.add(new BookInfo("00002", "浪漫满屋.txt", R.raw.text_book));
//                    bookLists.add(new BookInfo("00022", "爱在何方，家在何处.txt", R.raw.azhf));
//                    bookLists.add(new BookInfo("00003", "book.umd", R.raw.umd_book));
                    BookInfo netBook = new BookInfo("10002", "斗破苍穹");
                    netBook.author = "天蚕土豆";
                    netBook.netIndex = 873530;
                    netBook.cover = "https://bkimg.cdn.bcebos.com/pic/0ff41bd5ad6eddc448be31f537dbb6fd52663366?x-bce-process=image/watermark,g_7,image_d2F0ZXIvYmFpa2UxMTY=,xp_5,yp_5";
                    bookLists.add(netBook);
                }
                for (BookInfo book : bookLists) {
                    if (book.rawId != -1 && TextUtils.isEmpty(book.filePath)) {
                        InputStream is = BaseContext.getInstance().getResources().openRawResource(book.rawId);
                        String filePath = getBookFielPath(book.title);
                        if (FileUtils.copyFileToFile(filePath, is)) {
                            book.filePath = filePath;
                        }
                    }
                    if (TextUtils.isEmpty(book.cover)) {
                        if (!TextUtils.isEmpty(book.filePath)) {
                            if (book.filePath.endsWith(".epub")) {
                                EpubPlugin epubPlugin = new EpubPlugin(book.filePath);
                                try {
                                    epubPlugin.init();
                                    BookInfo decodeBook = epubPlugin.getBookInfo(book);
                                    book.id = decodeBook.id;
                                    book.title = decodeBook.title;
                                    book.author = decodeBook.author;
                                    book.publisher = decodeBook.publisher;

                                    InputStream inputStream = epubPlugin.getCoverStream();
                                    if (inputStream != null) {
                                        String fileName = getBookFielPath("cover" + book.filePath.hashCode());
                                        if (FileUtils.copyFileToFile(fileName, inputStream)) {
                                            book.cover = fileName;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (book.filePath.endsWith(".umd")) {
                                UmdPlugin umdPlugin = new UmdPlugin(book.filePath);
                                try {
                                    umdPlugin.init();
                                    book.title = umdPlugin.mTitle + ".umd";
                                    book.author = umdPlugin.mAuthor;
                                    Bitmap bitmap = umdPlugin.mBookCover;
                                    String fileName = getBookFielPath("cover" + book.filePath.hashCode());
                                    book.cover = BitmapUtil.saveBitmap(new File(fileName), bitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                isPreLoaded = true;
                if (iCallback != null) {
                    BaseContext.getHandler().post(() -> {
                        iCallback.onBookLoaded(bookLists);
                        iCallback = null;
                    });
                }
            }
        }.start();
    }

    public BookInfo decodeFile(String filePath) {
        BookInfo book = new BookInfo();
        book.id = filePath.hashCode() +"";
        book.filePath = filePath;
        book.title = filePath;
        if (filePath.endsWith(".epub")) {
            EpubPlugin epubPlugin = new EpubPlugin(filePath);
            try {
                epubPlugin.init();
                BookInfo decodeBook = epubPlugin.getBookInfo(book);
                book.id = decodeBook.id;
                book.title = decodeBook.title;
                book.author = decodeBook.author;
                book.publisher = decodeBook.publisher;

                InputStream inputStream = epubPlugin.getCoverStream();
                if (inputStream != null) {
                    String fileName = getBookFielPath("cover" + filePath.hashCode());
                    if (FileUtils.copyFileToFile(fileName, inputStream)) {
                        book.cover = fileName;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (filePath.endsWith(".umd")) {
            UmdPlugin umdPlugin = new UmdPlugin(filePath);
            try {
                umdPlugin.init();
                book.title = umdPlugin.mTitle + ".umd";
                book.author = umdPlugin.mAuthor;
                Bitmap bitmap = umdPlugin.mBookCover;
                String fileName = getBookFielPath("cover" + filePath.hashCode());
                book.cover = BitmapUtil.saveBitmap(new File(fileName), bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            book.title = new File(filePath).getName();
        }
        return book;
    }

    private String getBookFielPath(String fend) {
        String pathDir = BaseContext.getInstance().getCacheDir() + File.separator;
        String path = pathDir + "cache" + fend;
        File fileDir = new File(pathDir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return path;
    }
}
