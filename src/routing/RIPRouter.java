package routing;

import core.Connection;
import core.DTNHost;
import core.Settings;
import core.Message;
import routing.util.RoutingInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RIPRouter extends ActiveRouter{

    public static Integer MAX_HOP_COUNT = 15;

    public static final String RIP_ROUTER_NS = "RipRouter";

    private Map<DTNHost, Integer> routingTable = new HashMap<>();
    private List<DTNHost> neighbors = new ArrayList<>();

    public RIPRouter(Settings s) {
        super(s);
    }


    protected RIPRouter(RIPRouter r) {
        super(r);
    }


    @Override
    public void update() {
        super.update();

        if (!canStartTransfer() || isTransferring()) {
            return; // nothing to transfer or is currently transferring
        }

        // Try to send all first-hop unattached messages to all neighbors
        List<Message> messages = getMessagesWithNextHop(getHost());
        for (Message m : messages) {
            for (Connection con : getHost().getConnections()) {
                DTNHost neighbor = con.getOtherNode(getHost());

                // Don't send the message to the original destination or previous hop
                if (neighbor.equals(m.getTo()) || neighbor.equals(m.getFrom())) {
                    continue;
                }
                // Use the basic forwarding algorithm from ActiveRouter
                if (startTransfer(m, con) == RCV_OK) {
                    break;
                }
            }
        }
    }

    private List<Message> getMessagesWithNextHop(DTNHost host) {
        List<Message> messages = new ArrayList<>(host.getMessageCollection());
        List<Message> relevantMessages = new ArrayList<Message>();

        for (Message m : messages) {
            if (m.getTo().equals(host)) {
                relevantMessages.add(m);
            }
        }

        return relevantMessages;
    }


    @Override
    public MessageRouter replicate() {
        return new RIPRouter(this);
    }
}
