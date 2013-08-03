
public interface DefaultSocket {
    public void send(int type);
    public void send(int type, Object arg1);
    public void send(int type, Object arg1, Object arg2);
    public void send (Transporter send);
    public void disconnect();
}
