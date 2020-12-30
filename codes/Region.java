public class Region {

    private int regionX;
    private int regionY;
    private int regionInfo;
    private boolean regionScanned;
    private boolean ufoSuspected;

    // Getter methods //
    public Integer getRegionX() {
        return this.regionX;
    }
    
    public Integer getRegionY() {
        return this.regionY;
    }
    
    public Integer getRegionInfo() {
        return this.regionInfo;
    }
    
    public boolean getRegionScanned() {
        return this.regionScanned;
    }
    
    public boolean getUFOSuspected() {
        return this.ufoSuspected;
    }
    
    // Setter methods //
    public void setRegionX(int regionX) {
        this.regionX = regionX;
    }
    
    public void setRegionY(int regionY) {
        this.regionY = regionY;
    }
    
    public void setRegionInfo(int regionInfo) {
        this.regionInfo = regionInfo;
    }
    
    public void setRegionScanned(boolean regionScanned) {
        this.regionScanned = regionScanned;
    }
    
    public void setUFOSuspected(boolean ufoSuspected) {
        this.ufoSuspected = ufoSuspected;
    }
    
}
