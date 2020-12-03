public class SymbolTable {

    private String label;
    private String address;

    public SymbolTable(String address, String label) {
        this.address = address;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
