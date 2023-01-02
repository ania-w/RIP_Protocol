package routing;

import core.Settings;

public class RIPRouter extends ActiveRouter{

    public static Integer MAX_HOP_COUNT = 15;

    public RIPRouter(Settings s) {
        super(s);
    }

    protected RIPRouter(RIPRouter r) {
        super(r);
    }


    @Override
    public void update() {

    }

    @Override
    public MessageRouter replicate() {
        return new RIPRouter(this);
    }
}
