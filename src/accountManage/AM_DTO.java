package accountManage;

public class AM_DTO {
	
	// account 데이터베이스에 저장된 칼럼들( name - 이름, phone - 전화번호, accountNum - 계좌번호, money - 해당 계좌 잔액, pw - 해당 계좌 비밀번호
	
	private String name, pw;
	private int phone, accountNum, money;
	
	public AM_DTO() {}
	public AM_DTO(String name, int phone, int accountNum, int money, String pw) {
		this.name = name;
		this.phone = phone;
		this.accountNum = accountNum;
		this.money = money;
		this.pw = pw;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPw() {
		return pw;
	}
	public void setPw(String pw) {
		this.pw = pw;
	}
	public int getPhone() {
		return phone;
	}
	public void setPhone(int phone) {
		this.phone = phone;
	}
	public int getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(int accountNum) {
		this.accountNum = accountNum;
	}
	public int getMoney() {
		return money;
	}
	public void setMoney(int money) {
		this.money = money;
	}
	
}
