package routing;

import core.Connection;
import core.DTNHost;
import core.Settings;
import core.Message;


import java.util.*;

public class RIPRouter extends ActiveRouter{

    public static final int MAX_HOP_COUNT = 15;
    private Map<DTNHost, Integer> routingTable;
    private List<DTNHost> neighbors;

    public RIPRouter(Settings s) {
        super(s);
        routingTable = new HashMap<>();
        neighbors = new ArrayList<>();
    }


    protected RIPRouter(RIPRouter r) {
        super(r);
        this.routingTable = new HashMap<>(r.routingTable);
        this.neighbors = new ArrayList<>(r.neighbors);
    }


    @Override
    public void update() {
        super.update();

        if (!canStartTransfer() || isTransferring()) {
            return; // nothing to transfer or is currently transferring
        }

        updateRoutingTable();

        List<Message> messages = new ArrayList<>(getMessageCollection());

        for (Message m : messages) {
            DTNHost destination = m.getTo();
            Integer hopCount = routingTable.get(destination);
            for(Connection conn : destination.getConnections()) {
                if (hopCount != null && hopCount <= MAX_HOP_COUNT) {
                    // deliver the message
                    boolean success = startTransfer(m, conn) == RCV_OK;
                    if (success) {
                        transferDone(destination.getConnections().get(0));
                    }
                }
            }
        }

    }

    private void updateRoutingTable() {
        for (Connection conn : this.getHost().getConnections()) {
            DTNHost host = conn.getOtherNode(getHost());
            if (!this.routingTable.containsKey(host)) {
                this.routingTable.put(host, 1);
            } else {
                int hopCount = this.routingTable.get(host);
                if (hopCount < MAX_HOP_COUNT) {
                    this.routingTable.put(host, hopCount + 1);
                }
            }
        }
    }


    public List<DTNHost> getNeighbors() {
        return neighbors;
    }

    public Map<DTNHost, Integer> getRoutingTable() {
        return routingTable;
    }



    @Override
    public MessageRouter replicate() {
        return new RIPRouter(this);
    }
}
