package btree;

class Rid {
    int pageId;
    int tupleId;

    public Rid(int pageId, int tupleId){
        this.pageId = pageId;
        this.tupleId = tupleId;
    }

    public Rid(){
        this.pageId = 0;
        this.tupleId = 0;
    }

    public int getPageId(){
        return this.pageId;
    }

    public int getTupleId(){
        return this.tupleId;
    }
}