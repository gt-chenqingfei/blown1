package com.shuashuakan.android.data.api.model.im;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/26
 * Description:
 */
public class AppCustomMessage {
  /**
   * create_at : 1537432425893
   * data : {"type":"COMMENT"}
   * scene : RECEIVE_NEW
   * scope : NOTIFICATION
   * source : SYSTEM
   */

  private long create_at;
  private DataBean data;
  private String scene;
  private String scope;
  private String source;

  public long getCreate_at() {
    return create_at;
  }

  public void setCreate_at(long create_at) {
    this.create_at = create_at;
  }

  public DataBean getData() {
    return data;
  }

  public void setData(DataBean data) {
    this.data = data;
  }

  public String getScene() {
    return scene;
  }

  public void setScene(String scene) {
    this.scene = scene;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public static class DataBean {
    /**
     * type : COMMENT
     */

    private String type;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}
