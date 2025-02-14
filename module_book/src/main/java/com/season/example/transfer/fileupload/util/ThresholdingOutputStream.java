/*     */ package com.season.example.transfer.fileupload.util;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public abstract class ThresholdingOutputStream extends OutputStream
/*     */ {
/*     */   private final int threshold;
/*     */   private long written;
/*     */   private boolean thresholdExceeded;
/*     */ 
/*     */   public ThresholdingOutputStream(int threshold)
/*     */   {
/*  75 */     this.threshold = threshold;
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/*  92 */     checkThreshold(1);
/*  93 */     getStream().write(b);
/*  94 */     this.written += 1L;
/*     */   }
/*     */ 
/*     */   public void write(byte[] b)
/*     */     throws IOException
/*     */   {
/* 109 */     checkThreshold(b.length);
/* 110 */     getStream().write(b);
/* 111 */     this.written += b.length;
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/* 128 */     checkThreshold(len);
/* 129 */     getStream().write(b, off, len);
/* 130 */     this.written += len;
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 143 */     getStream().flush();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 158 */       flush();
/*     */     }
/*     */     catch (IOException ignored)
/*     */     {
/*     */     }
/*     */ 
/* 164 */     getStream().close();
/*     */   }
/*     */ 
/*     */   public int getThreshold()
/*     */   {
/* 178 */     return this.threshold;
/*     */   }
/*     */ 
/*     */   public long getByteCount()
/*     */   {
/* 189 */     return this.written;
/*     */   }
/*     */ 
/*     */   public boolean isThresholdExceeded()
/*     */   {
/* 202 */     return this.written > this.threshold;
/*     */   }
/*     */ 
/*     */   protected void checkThreshold(int count)
/*     */     throws IOException
/*     */   {
/* 221 */     if ((!this.thresholdExceeded) && (this.written + count > this.threshold))
/*     */     {
/* 223 */       this.thresholdExceeded = true;
/* 224 */       thresholdReached();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void resetByteCount()
/*     */   {
/* 234 */     this.thresholdExceeded = false;
/* 235 */     this.written = 0L;
/*     */   }
/*     */ 
/*     */   protected abstract OutputStream getStream()
/*     */     throws IOException;
/*     */ 
/*     */   protected abstract void thresholdReached()
/*     */     throws IOException;
/*     */ }

/* Location:           C:\Users\Administrator\Desktop\commons-io-2.4.jar
 * Qualified Name:     org.apache.commons.io.output.ThresholdingOutputStream
 * JD-Core Version:    0.6.0
 */