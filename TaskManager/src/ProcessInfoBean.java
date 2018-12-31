import javafx.beans.property.SimpleStringProperty;

public class ProcessInfoBean {
    private SimpleStringProperty name;
    private SimpleStringProperty pid;
    private SimpleStringProperty sessionName;
    private SimpleStringProperty sessionNum;
    private SimpleStringProperty memory;

    ProcessInfoBean(String name, String pid, String sessionName, String sessionNum, String memory) {
        this.name = new SimpleStringProperty(name);
        this.pid = new SimpleStringProperty(pid);
        this.sessionName = new SimpleStringProperty(sessionName);
        this.sessionNum = new SimpleStringProperty(sessionNum);
        this.memory = new SimpleStringProperty(memory);
    }

    public String getName() {
        return name.get();
    }

    public String getPid() {
        return pid.get();
    }

    public String getSessionName() {
        return sessionName.get();
    }

    public String getSessionNum() {
        return sessionNum.get();
    }

    public String getMemory() {
        return memory.get() + "K";
    }
}
