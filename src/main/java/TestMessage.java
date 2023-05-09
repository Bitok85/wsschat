import lombok.Data;

public class TestMessage {

    private String senderServerId;
    private String receiverServerId;
    private String text;

    public TestMessage(String senderServerId, String receiverServerId, String text) {
        this.senderServerId = senderServerId;
        this.receiverServerId = receiverServerId;
        this.text = text;
    }

    public String getSenderServerId() {
        return senderServerId;
    }

    public void setSenderServerId(String senderServerId) {
        this.senderServerId = senderServerId;
    }

    public String getReceiverServerId() {
        return receiverServerId;
    }

    public void setReceiverServerId(String receiverServerId) {
        this.receiverServerId = receiverServerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TestMessage{" +
                "senderServerId='" + senderServerId + '\'' +
                ", receiverServerId='" + receiverServerId + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
