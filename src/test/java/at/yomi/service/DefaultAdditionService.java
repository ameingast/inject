package at.yomi.service;

import at.yomi.Startable;

public class DefaultAdditionService implements AdditionService, Startable {
    public boolean started = false;

    @Override
    public int add(int x, int y) {
        return x + y;
    }

    @Override
    public boolean supports(String hint) {
        return "DefaultAdditionService".equals(hint);
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }
}
