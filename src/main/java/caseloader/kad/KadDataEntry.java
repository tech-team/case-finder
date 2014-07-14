package caseloader.kad;

public class KadDataEntry {
    private KadResponseItem item;
    private double cost;

    public KadDataEntry(KadResponseItem item, double cost) {
        item.splitSides();
        this.item = item;
        this.cost = cost;
    }

    public KadResponseItem getItem() {
        return item;
    }

    public double getCost() {
        return cost;
    }
}
