package accountManage;

import java.sql.Timestamp;

public class AM_Log_DTO {
	
	// 입금, 출금, 송금에 관한 기록들을 저장하는 account_log에 대한 칼럼들( id - 변동 기록 번호(1부터 차례로 생성),name - 이름, changedTime - 변동 시각(timestamp로 자동 생성), changedAmount(변동 금액, 입출금 및 송금 시 함수 내에서 체크))
	
	private Timestamp changedTime;
	private int changedAmount;

	public Timestamp getChangedTime() {
		return changedTime;
	}

	public void setChangedTime(Timestamp changedTime) {
		this.changedTime = changedTime;
	}

	public int getChangedAmount() {
		return changedAmount;
	}

	public void setChangedAmount(int changedAmount) {
		this.changedAmount = changedAmount;
	}
	
	public String toString() {
        return "[" +
                "변동 시각 : " + changedTime +
                ", 변동 금액 : " + changedAmount +
                ']';
        //기록들을 불러올 때 호출할 리스트의 형태 저장
    }
}
