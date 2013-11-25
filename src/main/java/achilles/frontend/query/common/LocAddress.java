package achilles.frontend.query.common;

public class LocAddress {
	private String province;
	private String city;
	private String addr_name;
	private String business;
	private String district;
	public LocAddress(String province,String city,String addr_name,String business,String district){
		this.province  = province;
		this.city = city;
		this.addr_name = addr_name;
		this.business = business;
		this.district = district;
	}
	public String getProvince() {
		return province;
	}
	public String getCity() {
		return city;
	}
	public String getAddr_name() {
		return addr_name;
	}
	public String getBusiness() {
		return business;
	}
	public String getDistrict() {
		return district;
	}
}
