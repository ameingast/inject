package at.yomi;

class ServiceWrapper {

    private Object instance;

    private boolean isStarted = false;

    ServiceWrapper(Object instance) {
        this.setInstance(instance);
    }

    public void stop() {
        if (isStarted) {
            if (getInstance() instanceof Startable) {
                ((Startable) getInstance()).stop();
            }
            isStarted = false;
        }
    }

    Object getInstance() {
        return instance;
    }

    void setInstance(Object instance) {
        this.instance = instance;
    }

    void start() {
        if (!isStarted) {
            if (getInstance() instanceof Startable) {
                ((Startable) getInstance()).start();
            }
            isStarted = true;
        }
    }

    boolean supports(String hint) {
        if (getInstance() instanceof Selectable) {
            return ((Selectable) getInstance()).supports(hint);
        }
        return false;
    }
}
