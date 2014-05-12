package sample.lockfreestate;

import de.ruedigermoeller.kontraktor.Actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ruedi on 12.05.14.
 */
public class StateContainingActor extends Actor {

    public static class DataRecord {
        String name;
        String description;
        int qty;
        double prc;

        public DataRecord(String name, String description, int qty, double prc) {
            this.name = name;
            this.description = description;
            this.qty = qty;
            this.prc = prc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public double getPrc() {
            return prc;
        }

        public void setPrc(double prc) {
            this.prc = prc;
        }

        public DataRecord createCopy() {
            return new DataRecord(getName(),getDescription(),getQty(),getPrc());
        }

        @Override
        public String toString() {
            return "DataRecord{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", qty=" + qty +
                    ", prc=" + prc +
                    '}';
        }
    }


    List lotsOfData;

    // setup some dummy data
    public void init() {
        lotsOfData = new ArrayList<DataRecord>();
        for ( int i = 0; i < 10000; i++) {
            lotsOfData.add(new DataRecord("NAME"+i,"no descr", i+5, i+5+Double.parseDouble("10."+i)));
        }
    }

    // define access interface for ActorRunable's thrown in from external
    // the getter should never be public, so it is accesisble only to the ActorRunable call
    // scheduled inside this actors thread.
    @Override protected DataAccess getActorAccess() {
        return () -> { return lotsOfData; };
    }

    public static interface DataAccess {
        List<DataRecord> getRecords();
    }

}
