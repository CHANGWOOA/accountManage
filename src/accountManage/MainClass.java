package accountManage;

import java.sql.Connection;
import java.util.List;
import java.util.Scanner;
import accountManage.AM_DAO;

public class MainClass {
	
	
	public static void main(String[] args) {
		
		DBConnect db = new DBConnect();
		Connection con = db.getConnect();
		
		Scanner input = new Scanner(System.in);
		int num;
		int accountNum=0, money, result;
		String pw;
		
		AM_DAO dao = new AM_DAO();

		while(true) {
			System.out.println("===================================");
			System.out.println("1. 로그인\n2. 신규 계좌 개설\n3. 종료");
			System.out.println("===================================");

			num = input.nextInt();
			switch(num) {
			case 1: //첫 시작 > 로그인
				
				System.out.println("계좌번호를 입력하세요");
				int existNum = input.nextInt();
				System.out.println("비밀번호를 입력하세요");
				String existPW = input.next();
				String loginResult = dao.loginAccount(existNum, existPW);
				System.out.println(loginResult);
				//로그인 성공하면 입금,출금,송금,잔액확인으로 이동
				if(loginResult.equals("로그인 완료")) {
					
					dao.saveUserInfoToFile(existNum);
					// 로그인 한 사람의 계좌번호를 제목, 비밀번호를 내용으로 하는 txt 파일 생성
					
					while(true) {
						// 로그인 후 시행 가능한 기능들
						System.out.println("\n==================================="); 
						System.out.println("1. 입금\n2. 출금\n3. 송금\n4. 잔액확인\n5. 기록확인\n6. 종료");
						System.out.println("===================================");
						int num1=input.nextInt();
						
						String doubleCheckPw = dao.readPw(existNum);
						// 메모장에서 비밀번호를 읽어와서, doubleCheckPw에 저장
						
						Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
							// 안 배운 거!
							// main 함수 종료 시 해당 스레드 자동 시행 예약
							@Override
							public void run() {
								dao.delTxt(existNum);
								// 해당 계좌번호의 txt 파일을 삭제 시키는 명령
							}			
						}));
						
						switch(num1) {
						case 1: // 첫 시작 > 로그인 > 입금 기능
							System.out.println("본인 확인을 위해 비밀번호를 입력하세요.");
							pw = input.next();
							
							if(doubleCheckPw==null || !pw.equals(doubleCheckPw)) {
								//유저가 입력한 비밀번호와 메모장에 저장된 비밀번호 재 확인
								System.out.println("비밀번호가 틀립니다. 다시 확인해주세요.");
								break;
							}
							System.out.println("입금하실 금액을 입력하세요.");
							money = input.nextInt();
							result = dao.deposit(existNum, money);
							//입금 함수 시행
							if(result == 1) {
								System.out.println("입금이 완료 되었습니다.");
							}
							break;
							
						case 2: // 첫 시작 > 로그인 > 출금 기능
							System.out.println("본인 확인을 위해 비밀번호를 입력하세요.");
							pw = input.next();
							
							if(doubleCheckPw==null || !pw.equals(doubleCheckPw)) {
								//유저가 입력한 비밀번호와 메모장에 저장된 비밀번호 재 확인
								System.out.println("비밀번호가 틀립니다. 다시 확인해주세요.");
								break;
							}
							System.out.println("출금하실 금액을 입력하세요.");
							money = input.nextInt();
							result = dao.withdraw(existNum, money);
							// 출금 함수 시행
							if(result == -1) {
								System.out.println("잔액이 부족합니다.");
							}else if(result == 0) {
								System.out.println("비밀번호가 틀립니다. 다시 확인해주세요.");
							}else {
								System.out.println("출금이 완료 되었습니다.");
							}
							break;
						case 3:  // 첫 시작 > 로그인 > 송금 기능
							System.out.println("계좌의 비밀번호를 입력하세요.");
							pw = input.next();
							if(doubleCheckPw==null || !pw.equals(doubleCheckPw)) {
								//유저가 입력한 비밀번호와 메모장에 저장된 비밀번호 재 확인
								System.out.println("비밀번호가 틀립니다. 다시 확인해주세요.");
								break;
							}
							
							AM_DTO dto = new AM_DTO();
							int inAcc, plusAcc, minus; //각각 송금할 계좌번호, 송금할 금액 변수
						
							inAcc = dao.readTitle(existNum); 
							// 메모장의 제목(현재 로그인 한 유저의 계좌번호)를 inAcc에 저장
							
							System.out.println("송금하실 금액을 입력하세요.");
							minus = input.nextInt(); //송금할 금액
							System.out.println("송금하실 계좌번호를 입력하세요.");
							plusAcc = input.nextInt(); //송금 계좌번호

							
							int result2 = dao.balCheck(inAcc); //기존 계좌 잔액체크: 계좌 번호입력받아서 잔액을 출력
							int result4 = dao.accCheck(plusAcc); //송금할 계좌번호가 존재하는지 확인
							
							if(minus>result2) {
								//금액이 기존계좌의 잔액보다 큰 경우
								System.out.println("잔액이 부족합니다");
							}else if(result4==0 || result4 ==-1){ 
								//계좌번호가 존재하지 않으면 0이나 -1 출력
								System.out.println("해당 계좌는 존재하지 않습니다.");
							}
							else {
								int result3= dao.transfer(minus, inAcc, plusAcc); 
								if(result3 ==1);{ //송금이 완료되면 1을 출력함
									System.out.println("송금이 완료되었습니다.");
									dto =dao.transferRe(inAcc, minus); 
									//게터를 사용하여 결과값을 출력
									System.out.println("송금 후 "+dto.getName()+"님의 잔액은 "+dto.getMoney()+"원 입니다.");
								}
							}
							break;
						case 4:  // 첫 시작 > 로그인 > 잔액 확인 기능
							int accountBalance = dao.checkAccount(existNum);
							System.out.println("잔액 : "+accountBalance+"원");
							break;
							
						case 5:  // 첫 시작 > 로그인 > 최근 거래 내역 호출 기능
							System.out.println("최근 거래 내역 10회 입니다.\n");
							int forHistory = dao.readTitle(existNum);
							// 메모장의 제목(현재 로그인 한 유저의 계좌번호)를 forHistory에 저장
							
							List<AM_Log_DTO> history = dao.getHistory(forHistory);
							// 불러올 자료형과 함수를 AM_Log_DTO에 저장
							
							System.out.println("==========최근 거래 내역 10회==========");
							for(AM_Log_DTO log : history) {
								System.out.println(log);
								// 최근 거래 내역 10회 호출 함수 시행
							}
							
							break;
							
						case 6: //종료
							System.out.println("종료합니다.");
							return;
							// 단순히 break로 [로그인 화면 + 로그인 후 기능] 이 두가지가 종료가 안 되어서, 
							// return으로 종료하면서 default로 이동하게 함. default 또한 종료.
						default:	
							break; 
						}
					}
				}
				
				break;
			case 2: //첫 시작 > 신규 계좌 개설
				System.out.println("=========================");
				System.out.println("신규 계좌를 개설합니다.\n성함을 적어주세요.");
				String name = input.next();
				
				System.out.println("사용하실 연락처를 적어주세요.");
				int phone = input.nextInt();
				
				System.out.println("사용하실 계좌번호를 적어주세요.(8자리)");
				accountNum = input.nextInt();
				
				System.out.println("사용하실 계좌번호의 잔액을 알려주세요.");
				money = input.nextInt();
				
				System.out.println("비밀번호를 설정해주세요.");
				pw = input.next();
				
				result = dao.newAccount(name, phone, accountNum, money, pw);
				// 입력받은 개인 정보들을 account 데이터베이서로 삽입 실행하는 함수 시행
				
				if(result == 0) {
					System.out.println("회원가입에 문제가 발생했습니다.\n메인메뉴로 돌아갑니다.");
				}else {
					System.out.println("신규 계좌 개설을 축하드립니다!");
					System.out.println("현재 계좌 번호는 " + accountNum + " 입니다.");
				}
				
				break;
			case 3: 
				System.exit(0);
				// 시스템 정상 종료 시행.
				break;
			}
		
		}
	}
}
