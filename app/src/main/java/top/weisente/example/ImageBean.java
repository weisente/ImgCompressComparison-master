package top.weisente.example;

class ImageBean {
  private String originArg;
  private String thumbArg;private String timeArg;
  private String image;


  public String getTimeArg() {
    return timeArg;
  }

  public void setTimeArg(String timeArg) {
    this.timeArg = timeArg;
  }

    public ImageBean(String originArg, String thumbArg, String timeArg, String image) {
        this.originArg = originArg;
        this.thumbArg = thumbArg;
        this.image = image;
        this.timeArg = timeArg;
    }

    String getOriginArg() {
    return originArg;
  }

  public void setOriginArg(String originArg) {
    this.originArg = originArg;
  }

  String getThumbArg() {
    return thumbArg;
  }

  public void setThumbArg(String thumbArg) {
    this.thumbArg = thumbArg;
  }

  String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }
}
