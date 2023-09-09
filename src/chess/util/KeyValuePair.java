package chess.util;

public class KeyValuePair<L, R> {

    private L key;

    private R value;

    public KeyValuePair(L key, R value) {
        this.key = key;
        this.value = value;
    }

    public L getKey() {
        return this.key;
    }

    public void setKey(L key) {
        this.key = key;
    }

    public R getValue() {
        return this.value;
    }

    public void setValue(R value) {
        this.value = value;
    }

}
