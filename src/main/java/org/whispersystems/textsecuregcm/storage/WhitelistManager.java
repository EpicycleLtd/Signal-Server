package org.whispersystems.textsecuregcm.storage;


public class WhitelistManager {

  private final Whitelist whitelist;

  public WhitelistManager(Whitelist whitelist) {
    this.whitelist = whitelist;
  }

  public boolean isInWhitelist(String number) {
    return this.whitelist.isInWhitelist(number, 1);
  }

  public boolean hasNumber(String number) {
    return whitelist.query(number);
  }

  public long insert(String number, int type) {
    return whitelist.insert(type, number);
  }

  public void remove(String number) {
    whitelist.delete(number);
  }

  public void update(String number, int type) {
    whitelist.update(type, number);
  }
}
