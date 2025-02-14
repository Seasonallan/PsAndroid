package com.ripple.client;

import android.annotation.SuppressLint;

import com.ripple.client.requests.Request;
import com.ripple.client.transport.TransportEventHandler;
import com.ripple.client.transport.WebSocketTransport;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

public class MockPair {
    RippledMock server = new RippledMock();

    public static class Scheduler {
        private PriorityQueue<Callback> queue;
        private int ms = 0;

        public Scheduler() {
            queue = new PriorityQueue<Callback>();
        }

        public class Callback implements Comparable<Callback> {
            long when;

            Runnable runnable;
            public Callback(Runnable runnable, long delay) {
                this.when = ms + delay;
                this.runnable = runnable;
            }

            @SuppressLint("NewApi")
            @Override
            public int compareTo(Callback o) {
                return Long.compare(when, o.when);
            }
        }

        public void schedule(long delay, Runnable runnable) {
            queue.add(new Callback(runnable, delay));
        }

        public void tick(int pass) {
            ms += pass;
            Iterator<Callback> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Callback next = iterator.next();
                if (next.when <= ms) {
                    try {
                        next.runnable.run();
                    } catch (Exception ignored) {
                        throw new RuntimeException(ignored);
                    } finally {
                        iterator.remove();
                    }
                } else {
                    break;
                }
            }
        }

    }

    // TODO a mock ScheduledExecutorService?
    // TODO make client abstract?
    public static class MockClient extends Client {
        public Scheduler scheduler;

        public MockClient(WebSocketTransport ws) {
            super(ws);
        }
        @Override
        protected void prepareExecutor() {
            service = null;
        }
        @Override
        public void run(Runnable runnable) {
            runnable.run();
        }
        @Override
        public void onMessage(JSONObject msg) {
            onMessageInClientThread(msg);
        }

        @Override
        public void schedule(long ms, Runnable runnable) {
            // We have to put up with this until we restructure this
            // a better way
            if (scheduler == null) {
                scheduler = new Scheduler();
            }
            scheduler.schedule(ms, runnable);
        }
    }

    MockClient client = new MockClient(server.ws);

    public MockPair connect() {
        client.connect("wss://this.doesnt.matter.com");
        client.scheduler.tick(50);
        server.connect();
        return this;
    }

    public static class Message {
        JSONObject msg;
        boolean client;
        int n;

        public Message(JSONObject msg, boolean client, int n) {
            this.msg = msg;
            this.client = client;
            this.n = n;
        }

        public Request getRequest(Client c) {
            return c.requests.get(msg.optInt("id", -1));
        }

    }

    public static class RippledMock {
        ArrayList<Message> messages;
        ArrayList<Message> archived;

        class MockSocket implements WebSocketTransport, TransportEventHandler {
            TransportEventHandler handler;
            boolean connected;

            @Override
            public void setHandler(TransportEventHandler events) {
                handler = events;
            }

            @Override
            public void sendMessage(JSONObject msg) {
                messages.add(new Message(msg, true, messages.size()));
            }

            @Override
            public void connect(URI url) {
                connected = true;
            }

            @Override
            public void disconnect() {
                connected = false;
            }

            @Override
            public void onMessage(JSONObject msg) {
                handler.onMessage(msg);
            }

            @Override
            public void onConnecting(int attempt) {
                handler.onConnecting(attempt);
            }

            @Override
            public void onDisconnected(boolean willReconnect) {
                handler.onDisconnected(willReconnect);
            }

            @Override
            public void onError(Exception error) {
                handler.onError(error);
            }

            @Override
            public void onConnected() {
                handler.onConnected();
            }
        }

        MockSocket ws;

        public RippledMock() {
            ws = new MockSocket();
            messages = new ArrayList<Message>();
            archived = new ArrayList<Message>();
        }

        public void connect() {
            if (ws.connected) {
                ws.onConnecting(0);
                ws.onConnected();
            }
        }
        public void disconnect() {
            if (ws.connected) {
                ws.onDisconnected(false);
            }
        }
        public void sendMessage(JSONObject json) {
            ws.onMessage(json);
        }

        public ArrayList<Message> unreadMarked() {
            ArrayList<Message> cleared =  new ArrayList<Message>(messages);
            messages.clear();
            archived.addAll(cleared);
            return cleared;
        }
        public ArrayList<Message> unread() {
            return messages;
        }

        public Message popMessage() {
            if (messages.size() > 0) {
                Message popped = messages.remove(messages.size() - 1);
                archived.add(popped);
                return popped;
            } else {
                return null;
            }
        }

        public void respond(Request request, String status, JSONObject result) {
            JSONObject response = new JSONObject();

            try {
                response.put("result", result);
                response.put("id", request.id);
                response.put("status", status);
                response.put("type", "response");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendMessage(response);
        }

        public void respondSuccess(Request request, JSONObject result) {
            respond(request, "success", result);
        }

        public void respondSuccess(Request request, String result) {
            respondSuccess(request, Client.parseJSON(result));
        }
    }
}
