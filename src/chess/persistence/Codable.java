package chess.persistence;

// An interface that represents any object that is encodable with a certain type of representation.
public interface Codable {

    /**
     * @return A serialized object ready to be written.
     * */
    Object encode();

}
