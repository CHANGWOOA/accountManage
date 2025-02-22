package accountManage;

import java.sql.Connection;
import java.sql.DriverManager;


public class DBConnect {
public static Connection getConnect() {
		
		Connection con = null;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			//ojdbc.jar호출
			System.out.println("드라이브 로드 성공");

			// 찬교 DB 접속 시 ip 주소 필요하면 : 192.168.51.96

			String id, pwd, url;
			id = "c##eclip"; 
			pwd = "1234";
			//해당 데이터베이스의 아이디 및 비밀번호 저장
			
			url = "jdbc:oracle:thin:@192.168.51.96:1521:xe";
			con = DriverManager.getConnection(url, id, pwd);
			// 데이터베이스의 호스트인 PC의 IP주소 저장
			System.out.println("연결 성공!");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}
}
