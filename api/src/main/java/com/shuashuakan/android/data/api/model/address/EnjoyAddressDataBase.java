package com.shuashuakan.android.data.api.model.address;

import android.util.SparseArray;
import com.shuashuakan.android.ui.address.AddressDistrict;
import java.util.List;


public class EnjoyAddressDataBase {
  private List<AddressProvince> provinceList;
  private SparseArray<List<AddressCity>> cityListArray;
  private SparseArray<List<AddressDistrict>> districtArray;

  public List<AddressProvince> getProvinceList() {
    return provinceList;
  }

  public void setProvinceList(List<AddressProvince> provinceList) {
    this.provinceList = provinceList;
  }

  public SparseArray<List<AddressCity>> getCityListArray() {
    return cityListArray;
  }

  public void setCityListArray(SparseArray<List<AddressCity>> cityListArray) {
    this.cityListArray = cityListArray;
  }

  public SparseArray<List<AddressDistrict>> getDistrictArray() {
    return districtArray;
  }

  public void setDistrictArray(SparseArray<List<AddressDistrict>> districtArray) {
    this.districtArray = districtArray;
  }
}
