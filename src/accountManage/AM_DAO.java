	package accountManage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import accountManage.AM_Log_DTO;
import accountManage.AM_DTO;

public class AM_DAO {
	Connection con;
	PreparedStatement ps;
	ResultSet rs;
	
	public AM_DAO() {
		con = DBConnect.getConnect();
	}
	
	static String forFilePath = "C:\\Users\\IT\\java_cookie\\";
	// 계좌번호, 비밀번호를 포함한 쿠키파일을 저장할 경로 지정(PC별 로컬 지정 필요)
	
	public int newAccount(String name, int phone, int accountNum, int money, String pw) {
		//새 계좌를 개설하는 함수
		String sql = "insert into account values(?,?,?,?,?)";
		int result = 0;
		
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			ps.setInt(2, phone);
			ps.setInt(3, accountNum);
			ps.setInt(4, money);
			ps.setString(5, pw);
			result = ps.executeUpdate();
			// account 데이터베이스에 필요한 정보들을 삽입하는 쿼리문 실행
			
		} catch (Exception e) {
			System.out.println("동일한 계좌를 입력해 문제가 발생했습니다.");
			//이미 계좌가 존재할 시 문제 발생
			e.printStackTrace();
		}
		
		return result;
	}
	public int checkAccount(int accountNum) {
		// 계좌의 잔액을 호출하는 함수
		int accountBalance = 0;
		String sql = "select * from account where accountNum=?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, accountNum);
			rs = ps.executeQuery();
			//입력한 계좌번호의 잔액을 호출하는 쿼리문
			if(rs.next()) {
				accountBalance = rs.getInt("money");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accountBalance;
		//데이터베이스에서 해당 계좌번호의 잔액 반환
	}
	public void saveUserInfoToFile(int accountNum) {
	    //유저가 로그인 할 시, 계좌번호를 제목으로 하는 비밀번호 내용의 txt 파일 생성
	    String sql = "SELECT pw FROM account WHERE accountNum=?";
	    try {
	        ps = con.prepareStatement(sql);
	        ps.setInt(1, accountNum);
	        rs = ps.executeQuery();
	        //입력한 계좌번호의 비밀번호를 호출하는 쿼리문
	        
	        if (rs.next()) {
	        	String pw = rs.getString("pw");
	            String filePath = forFilePath + accountNum + ".txt"; 
	            //사용자 계좌번호를 기반으로 하는 txt파일이름 저장
	            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            
	            	writer.write(pw);
	            	//입금, 출금, 송금 시 비밀번호 재확인을 위한 사용자정보를, 계좌번호에 해당하는 비밀번호 저장 ex) 0000
	            }
	        }
	    } catch (Exception e) {
	    	System.out.println("오류 발생: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	
	
	public String loginAccount(int existNum, String existPW) {
		//계좌번호와 비밀번호를 입력해 로그인 하는 함수
		String sql = "select * from account where accountNum=?";
		
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, existNum);
			rs = ps.executeQuery();
			//입력받은 계좌번호의 데이터베이스 정보 호출하는 쿼리문
			if(rs.next()) {
				if(rs.getString("pw").equals(existPW)) {
					//입력받은 계좌와 비밀번호를 데이터베이스 정보과 비교 하여 로그인
					saveUserInfoToFile(existNum); 
					//로그인 성공시 해당 사용자정보 저장
					return "로그인 완료";
				}
				else {
					return "비밀번호가 틀렸습니다.";
				}
			}
			else {
				return "존재하지 않는 계좌입니다.";
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		  return "로그인 오류";
		}
	}
	
	public String readPw(int accountNum) { 
		//생성한 계좌번호 이름의 txt파일의 내용인 비밀번호를 읽어오는 함수
	    String filePath = forFilePath + accountNum + ".txt"; 
	    //txt파일의 경로 + txt파일의 제목
	    String password = "";

	    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	        password = reader.readLine(); 
	        //파일 경로의 대상을 읽고, 읽은 결과를 password에 저장
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return password;
	    // 결과적으로 txt파일의 내용물을 반환
	}
	
	public int readTitle(int accountNum) {
		//생성한 계좌번호 이름의 txt파일의 제목을 읽어오는 함수
		String filePath = forFilePath + accountNum + ".txt"; 
	    File file = new File(filePath);
	    String fileName = file.getName();
	    int title = Integer.parseInt(fileName.substring(0, fileName.lastIndexOf(".")));
	    // 해당 경로의 txt파일의 제목을, "."기준 앞 부분만 정수 title에 저장
	    return title; 
	    // 해장 경로의 txt 파일을, "."기준 앞 부분만 반환
	}
	

	public int deposit(int existNum, int money) {
		//로그인 한 계좌를 대상으로 현금을 입금하는 함수
	    String updateSql = "UPDATE account SET money = money + ? WHERE accountNum = ?";
	    String logSql = "INSERT INTO account_log (name, changedAmount) VALUES (?, ?)";
	    int result = 0;
	    String name = null;
	    // 쿼리문 실행 결과 및 대상의 이름 초기화

	    try {
	        String nameSql = "SELECT name FROM account WHERE accountNum = ?";
	        
	        try (PreparedStatement ps = con.prepareStatement(nameSql)) {
	            ps.setInt(1, existNum);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) {
	                name = rs.getString("name");
	    		    // 해당 함수 실행 성공 시, account 에서 name을 참조해, 로그를 account_log로 보내는 쿼리문
	                // 해당 부분은 name만 참조함.
	            }
	        }
	        
	        try (PreparedStatement ps = con.prepareStatement(updateSql)) {
	            ps.setInt(1, money);
	            ps.setInt(2, existNum);
	            result = ps.executeUpdate();
	            //계좌번호에 현금을 입금하는 쿼리문 실행
	        }

	        if (result > 0 && name != null) {
	            try (PreparedStatement ps = con.prepareStatement(logSql)) {
	                ps.setString(1, name);
	                ps.setInt(2, money);
	                ps.executeUpdate();
	                //함수 실행 성공 시, 로그를 account_log로 전송
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("입금 중 오류가 발생했습니다.");
	        e.printStackTrace();
	    }
	    
	    return result;
	}
	
	public int withdraw(int existNum, int amount) {
		//계좌번호에서 현금을 출금하는 함수
		String sqlCheckBalance = "select money from account where accountNum = ?";
		String sqlWithdraw = "update account set money = money - ? where accountNum = ?";
		// 현금을 인출하고 update하는 쿼리문
		String logSql = "INSERT INTO account_log (name, changedAmount) VALUES (?, ?)";
		
		int result = 0;
		String name = null;
		// 쿼리문 실행 결과 및 대상의 이름 초기화
		
		try {
	        ps = con.prepareStatement(sqlCheckBalance);
	        ps.setInt(1, existNum);
	        rs = ps.executeQuery();
			// 입력한 계좌번호에서 money를 확인하는 쿼리문
	        
	        if (rs.next()) {
	            int moneyNow = rs.getInt("money");
	            if (moneyNow >= amount) {
	                ps = con.prepareStatement(sqlWithdraw);
	                ps.setInt(1, amount);
	                ps.setInt(2, existNum);
	
	                result = ps.executeUpdate();
	                // 계좌에 잔액이 충분할 시, 현금을 인출(update)하는 쿼리문 시행

	                if (result > 0) {
	                    String nameSql = "SELECT name FROM account WHERE accountNum = ?";
	                    try (PreparedStatement namePs = con.prepareStatement(nameSql)) {
	                        namePs.setInt(1, existNum);
	                        ResultSet nameRs = namePs.executeQuery();
	                        if (nameRs.next()) {
	                            name = nameRs.getString("name");
	                           // 계좌번호의 이름을 참조하는 쿼리문
	                        }
	                    }
	                    
	                    if (name != null) {
	                        ps = con.prepareStatement(logSql);
	                        ps.setString(1, name);
	                        ps.setInt(2, -amount);  // 출금이므로 음수로 기록
	                        ps.executeUpdate();
	                        // 인출 성공 시, account_log로 기록 전송
	                    }
	                }
	            } else {
	                result = -1; // 잔액 부족
	            }
	        }
	    } catch (Exception e) {
	        System.out.println("출금 중 오류가 발생했습니다.");
	        e.printStackTrace();
	    }
		return result;
	}


	public int balCheck(int Account) { //계좌 잔액 체크
		String sqlBalCheck = "select money from account where accountNum =?";
		int balmoney=0;
	
		try {	
			ps = con.prepareStatement(sqlBalCheck);
			ps.setInt(1, Account);
			rs = ps.executeQuery();
			//계좌의 잔액을 체크하는 쿼리문 시행
			if(rs.next()) {
				balmoney = rs.getInt("money");
				//계좌의 잔액을 확인 후, balmoney에 저장
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return balmoney;
		//계좌의 잔액 반환
	}
	
	public int accCheck(int plusAcc) { //입력받은 계좌번호가 존재하는지 체크
		String sqlAcc = "select accountNum from account where accountNum = ?";
		int result=-1;
		try {
			ps = con.prepareStatement(sqlAcc);
			ps.setInt(1, plusAcc);
			rs = ps.executeQuery();
			//계좌번호가 존재하는지 체크하는 쿼리문 시행
			if(rs.next()) {
				result = rs.getInt("accountNum");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result; //존재하는 경우에는 해당 계좌번호 출력, 없으면 0혹은 -1 출력.
	}

	public int transfer(int minus, int inAcc, int plusAcc) { //송금 실행 코드
	    String sqlMin = "UPDATE account SET money = (money - ?) WHERE accountNum = ?";
	    //기존 계좌에서 송금할 금액만큼 차감하여 기존 금액을 업데이트
	    String sqlPlus = "UPDATE account SET money = (money + ?) WHERE accountNum = ?";
	    //송금할 계좌의 잔액에 송금할 금액을 더하여 기존 금액을 업데이트
	  
	    String logSql = "INSERT INTO account_log (name, changedAmount) VALUES (?, ?)"; //찬교님 설명예정
	   
	    //초기화
	    int result1 = 0;
	    int result2 = 0;
	    String senderName = null;
	    String receiverName = null;

	    try {
	      
	        String nameSqlSender = "SELECT name FROM account WHERE accountNum = ?";
	        ps = con.prepareStatement(nameSqlSender);
	        ps.setInt(1, inAcc);
	        ResultSet rs = ps.executeQuery();
	        
	        //돈을 송금하는 사람의 이름을 호출하는 쿼리문 시행
	        
	        if (rs.next()) {
	            senderName = rs.getString("name");
	            // 쿼리문의 결과 셋의 이름을 senderName 에 저장
	        }

	        ps = con.prepareStatement(nameSqlSender);
	        ps.setInt(1, plusAcc);
	        rs = ps.executeQuery();
	        // 돈을 받는 사람의 이름을 호출해 rs 에 저장
	        if (rs.next()) {
	            receiverName = rs.getString("name");
	            // 돈을 받는 사람의 이름을 receiverName에 저장
	        }

	        // 송금 처리
	        ps = con.prepareStatement(sqlMin); //송금 실행하는 코드인데 result1 결과값을 사용하지는 않음.
	        ps.setInt(1, minus);
	        ps.setInt(2, inAcc);
	        result1 = ps.executeUpdate();

	        ps = con.prepareStatement(sqlPlus); //송금받을 계좌의 금액이 업데이트되면 1 return;
	        ps.setInt(1, minus);
	        ps.setInt(2, plusAcc);
	        result2 = ps.executeUpdate();


	        if (result1 > 0 && result2 > 0) {
	            if (senderName != null) {
	                ps = con.prepareStatement(logSql);
	                ps.setString(1, senderName);
	                ps.setInt(2, -minus);
	                ps.executeUpdate();
	                // 해당 송금 성공 실행 시 돈을 보내는 사람과, 송금한 금액을 account_log로 전송
	            }
	            if (receiverName != null) {
	                ps = con.prepareStatement(logSql);
	                ps.setString(1, receiverName);
	                ps.setInt(2, minus);
	                ps.executeUpdate();
	                // 해당 송금 성공 실행 시 돈을 받는 사람과, 받은 금액을 account_log로 전송
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return result2; //송금결과가 "1행이 변경되었습니다"이므로 1을 return;
	}

	public AM_DTO transferRe(int accountNum, int minus) {//송금결과확인
		AM_DTO dto = new AM_DTO(); //private 변수들을 사용할 것이므로 dto 사용
		
		String moneyRe = "select money from account where accountNum = ?";
		String nameSer = "select name from account where accountNum =?";
		int currentMoney = 0;
		String nameSerched = null;
		try {
			//해당 계좌의 잔액을 불러오는 코드
			ps = con.prepareStatement(moneyRe); 
			ps.setInt(1, accountNum);
			rs = ps.executeQuery();
			
			
			if(rs.next()) {
			currentMoney = rs.getInt("money"); //위 메소드에서 업데이트한 money를 출력
			}
			//해당 계좌의 이름을 불러오는 코드
			ps = con.prepareStatement(nameSer);
			ps.setInt(1, accountNum);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				nameSerched = rs.getString("name");
			}
			//DTO세터를 사용해서 각각 계좌번호, 이름, 잔액을 세팅해준다.
			dto.setAccountNum(accountNum);
			dto.setMoney(currentMoney);
			dto.setName(nameSerched);
			}
			
		 catch (Exception e) {
			e.printStackTrace();
		}
		
		return dto;
	}
	
	public static void delTxt(int accountNum) {
        String filePath = forFilePath + accountNum + ".txt";
        Path path = Path.of(filePath);

        try {
            if (Files.exists(path)) {
                Files.delete(path);
                // 아까 로그인 할 때 생성했던 txt파일을 삭제하는 함수.
            } 
        } catch (IOException e) {
        }
    }
	
	public List<AM_Log_DTO> getHistory(int accountNum) {
        List<AM_Log_DTO> history = new ArrayList<>();
        // account_log를 불러올 때, 자료형을 list로 받아오는 함수.
        String getNameSql = "SELECT name FROM account WHERE accountNum = ?";
        //account 에서 name을 참조하는 쿼리문
        String getLogSql = "SELECT changedTime, changedAmount FROM account_log WHERE name = ? ORDER BY changedTime DESC FETCH FIRST 10 ROWS ONLY";
        // account_log에서 변동 시각, 변동 금액을 최근 10회만 호출하는 쿼리문
        try {
            String name = null;
            try (PreparedStatement ps = con.prepareStatement(getNameSql)) {
                ps.setInt(1, accountNum);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        name = rs.getString("name");
                        // account를 참조해 name을 가져옴
                    }
                }
            }
            if (name != null) {
                try (PreparedStatement ps = con.prepareStatement(getLogSql)) {
                    ps.setString(1, name);
                    //참조한 이름에 대해,
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            AM_Log_DTO log = new AM_Log_DTO();
                            log.setChangedTime(rs.getTimestamp("changedTime"));
                            log.setChangedAmount(rs.getInt("changedAmount"));
                            history.add(log);
                            // 불러온 account_log에 변동 시각, 변동 금액을 list(AM_Log_DTO) 에 저장
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL 오류: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return history;
        // 계좌번호의 이름에 해당하는 변동시각, 변동 금액을 최근 10회를 리스트 형태 호출
    }
}


	
	
	
	
