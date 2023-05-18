package controll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.MemberVO;
import service.IMemberService;
import util.ConnectionSingletonHelper;

public class MemberController implements IMemberService {
    private static Connection conn;
    private static BufferedReader br;
    private StringBuilder sb;
    private SimpleDateFormat sdf;
    private MemberVO vo;
    private PreparedStatement pstmtInsertMember, pstmtSelectMemberValid, pstmtUpdateMemberLoginInfo, pstmtSelectMember, pstmtUpdateMember, pstmtDeleteMember;
    private final String sqlInsertMember = "INSERT INTO MEMBER (member_id, member_name, member_pwd, member_phone, member_birthday) VALUES(?,?,?,?,?)", 
            sqlSelectMemberValid = "SELECT MEMBER_ID,MEMBER_PWD,MEMBER_VALID FROM MEMBER WHERE MEMBER_ID=? AND MEMBER_PWD=?",
            sqlUpdateMemberLoginInfo = "UPDATE MEMBER SET MEMBER_VALID= ? WHERE MEMBER_ID = ?",
            sqlSelectMember = "SELECT member_id, member_name,member_phone,member_birthday FROM MEMBER WHERE MEMBER_ID=?",
            sqlUpdateMember = "UPDATE MEMBER SET MEMBER_NAME=?, MEMBER_PWD=?,MEMBER_PHONE=?,MEMBER_BIRTHDAY=? WHERE MEMBER_ID=?",
            sqlDeleteMember = "UPDATE MEMBER SET MEMBER_VALID=3 WHERE MEMBER_ID=?";
    
    public MemberController() throws Exception {
        conn = ConnectionSingletonHelper.getConnection();
        br = new BufferedReader(new InputStreamReader(System.in));
        sb = new StringBuilder();
    }
  
    @Override
    public void register() throws IOException, NoSuchAlgorithmException {
      SHA256 sha256 = new SHA256();
      	try {
      		
      		System.out.println("영문자와 숫자를 조합하여 5자이상 19자 이하로 입력하십시오");
       		System.out.print("ID: ");
       		String member_Id = null;
	        while(true) {
	        	
	        	member_Id = br.readLine();
			    String mid = "^[a-z]+[a-z0-9]{5,19}$";
			    boolean account = Pattern.matches(mid, member_Id);
			    	
			    if (!account) {
		            System.out.println("잘못된 입령양식 입니다. 다시입력하십시오");
				    System.out.print("ID: ");
				    continue;
				}
			break;
			}
        
		    System.out.println("최소 8 자, 하나 이상의 문자와 하나의 숫자,특수문자 !,@,$,!,%,*,#,^,?,& 를 이용하여  입력하십시오");
	        System.out.print("PassWord: ");
	        String member_Pwd = null;//최소 8 자, 하나 이상의 문자와 하나의 숫자,
	        while(true) {
	        	member_Pwd = br.readLine();
		        String mpwd= "^(?=.*[a-zA-z])(?=.*[0-9])(?=.*[$`~!@$!%*#^?&\\(\\)-_=+]).{8,16}$";
		        boolean password = Pattern.matches(mpwd, member_Pwd);
		        
		        if (!password) {
		            System.out.println("잘못된 입령양식 입니다. 다시입력하십시오");
		            System.out.print("PassWord: ");
		            continue;
		        }
		        break;
	        }
        
        System.out.print("Name: ");
        String member_Name = null;
        while(true) {
        	member_Name = br.readLine();
	        String mnf= "(^[a-zA-Zㄱ-힣][a-zA-Zㄱ-힣 ]*$)";
	        boolean nameformat = Pattern.matches(mnf, member_Name);
	        
	        if (!nameformat) {
	            System.out.println("잘못된 이름형식 입니다.");
	            System.out.print("Name: ");
	            continue;
	        }
	        break;
        }
        
        System.out.print("Phone: ");
	    String member_Phone = null; 
        while(true) {
	       member_Phone = br.readLine();
	        String mpho = "(\\d{2,3})(\\d{3,4})(\\d{4})";
	        boolean phonenumber = Pattern.matches(mpho, member_Phone);
	        
	        if (!phonenumber) {
	            System.out.println("잘못된 입령양식 입니다. 다시입력하십시오");
	            System.out.print("Phone: ");
	            continue;
	        }
        break;
        }
        
        System.out.print("Birthday: ");
        String member_Birthday = null;
        while(true) {
	        member_Birthday = br.readLine();
	        String mbd = "([0-9]{2}(0[1-9]|1[0-2])(0[1-9]|[1,2][0-9]|3[0,1]))";
	        boolean memberbirthday = Pattern.matches(mbd, member_Birthday);
	        
	        if (!memberbirthday) {
	            System.out.println("잘못된 입령양식 입니다. 다시입력하십시오");
	            System.out.print("Birthday: ");
	            continue;
	        }
	        break;
        }
        
        pstmtInsertMember = conn.prepareStatement(sqlInsertMember);
        pstmtInsertMember.setString(1, member_Id);
        pstmtInsertMember.setString(2, member_Name);
        pstmtInsertMember.setString(3, sha256.encrypt(member_Pwd));
        pstmtInsertMember.setString(4, member_Phone);
        pstmtInsertMember.setString(5, member_Birthday);

        pstmtInsertMember.executeUpdate();// 값 저장
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public MemberVO login() throws IOException {
    	 SHA256 sha256 = new SHA256();
    	 vo = new MemberVO();

      try {
        System.out.print("ID: ");
        String member_Id = br.readLine();
        System.out.print("PassWord: ");
        String member_Pwd = br.readLine();
        pstmtSelectMemberValid = conn.prepareStatement(sqlSelectMemberValid);
        pstmtSelectMemberValid.setString(1, member_Id);
        pstmtSelectMemberValid.setString(2, sha256.encrypt(member_Pwd));
        ResultSet rs = pstmtSelectMemberValid.executeQuery();

        if (!rs.isBeforeFirst()) {
          System.out.println("로그인이 되지않았습니다.");
          return null;
        }

        while (rs.next()) {
          vo.setMember_id(rs.getString(1));
          vo.setMember_pwd(rs.getString(2));
          vo.setMember_valid(rs.getInt(3));
        }
        pstmtUpdateMemberLoginInfo = conn.prepareStatement(sqlUpdateMemberLoginInfo );
        pstmtUpdateMemberLoginInfo.setInt(1, 1);
        pstmtUpdateMemberLoginInfo.setString(2, vo.getMember_id());
        pstmtUpdateMemberLoginInfo.executeUpdate();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {

      }
      System.out.println("로그인이 되었습니다.");
      return vo;
    }

    @Override
    public void logout() {
      try {
        pstmtSelectMemberValid = conn.prepareStatement(sqlSelectMemberValid);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {

      }
    }

    @Override
    public void myProfile() throws IOException {
        sb.setLength(0);
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            System.out.println("===================회원정보 보기======================");
            pstmtSelectMember = conn.prepareStatement(sqlSelectMember);
            pstmtSelectMember.setString(1, vo.getMember_id()); // 사용자 직접입력 X

            ResultSet rs = pstmtSelectMember.executeQuery();
            sb.append("--------------------┳---------------------------------").append("\n");
            sb.append(String.format("%-17s|%-6s|%-8s|%s", "아이디", "이름", "전화번호", "생일")).append("\n");
            sb.append("--------------------+---------------------------------").append("\n");
            while (rs.next()) {
                int len = 20 - (rs.getString(1).length() - 1) / 3;
                sb.append(String.format("%-" + len + "s|%-5s|%-12s|%-8s", rs.getString(1), rs.getString(2),
                        rs.getString(3), sdf.format(rs.getDate(4)))).append("\n");
                sb.append("--------------------┸---------------------------------").append("\n");
            }
            System.out.println(sb);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void editProfile() throws IOException {
        SHA256 sha256 = new SHA256();
        try {
            System.out.println("===================회원정보 수정======================");
            System.out.println("회원 확인을 위해 비밀번호를 입력해주세요 ");
            System.out.print("비밀번호: "); String pwd = sha256.encrypt(br.readLine());
            
            pstmtSelectMember = conn.prepareStatement(sqlSelectMemberValid);
            pstmtSelectMember.setString(1, vo.getMember_id());
            pstmtSelectMember.setString(2, pwd);
            ResultSet rs = pstmtSelectMember.executeQuery();
            if(!rs.isBeforeFirst()) {
                System.out.println("비밀번호가 틀렸습니다.");
                return;
            }
            
            pstmtUpdateMember = conn.prepareStatement(sqlUpdateMember);
            pstmtUpdateMember.setString(5, vo.getMember_id());
            System.out.print("이름: ");
            pstmtUpdateMember.setString(1, br.readLine());
            System.out.print("비밀번호: ");
            pstmtUpdateMember.setString(2, sha256.encrypt(br.readLine()));
            System.out.print("전화번호: ");
            pstmtUpdateMember.setString(3, br.readLine());
            System.out.print("생일: ");
            pstmtUpdateMember.setString(4, br.readLine());

            int result = pstmtUpdateMember.executeUpdate();
            System.out.println(result + "개 업데이트 성공");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMember() throws IOException {
        SHA256 sha256 = new SHA256();
        try {
            System.out.println("=====================회원 탈퇴========================");
            System.out.println("회원 확인을 위해 비밀번호를 입력해주세요 ");
            System.out.print("비밀번호: "); String pwd = sha256.encrypt(br.readLine());
            
            pstmtSelectMember = conn.prepareStatement(sqlSelectMemberValid);
            pstmtSelectMember.setString(1, vo.getMember_id());
            pstmtSelectMember.setString(2, pwd);
            ResultSet rs = pstmtSelectMember.executeQuery();
            if(!rs.isBeforeFirst()) {
                System.out.println("비밀번호가 틀렸습니다.");
                return;
            }
            
            pstmtDeleteMember = conn.prepareStatement(sqlDeleteMember);
            pstmtDeleteMember.setString(1, vo.getMember_id());
            
            int result = pstmtDeleteMember.executeUpdate();
            System.out.println(result<1?"다시 시도해주세요":"탈퇴 처리되었습니다");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

	public class SHA256 {

		public String encrypt(String text) throws NoSuchAlgorithmException {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(text.getBytes());

			return bytesToHex(md.digest());
		}

		private String bytesToHex(byte[] bytes) {
			StringBuilder builder = new StringBuilder();
			for (byte b : bytes) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		}

	}

	public class TelTest {
		public static String telNumber(String number) {
			// 전화번호 정규표현식으로 제한
			String regEx = "(\\d{2,3})(\\d{3,4})(\\d{4})";

			if (!Pattern.matches(regEx, number)) {
				System.out.println("에러 1 : 형식 오류 ====> " + number.toString());
				return null;
			}

			// 지역번호가 02이면서 9자리 수일 때 == not error
			if (number.substring(0, 2).contains("02") && number.length() == 9) {
				return number.replaceAll(regEx, "$1-$2-$3"); // 출력 xxx-xxx-xxxx
			}

			// 지역번호 02를 제외한 번호 (070,031,064 ...) 가 9자리 일 때 == > 에러
			else if (number.length() == 9) {
				System.out.println("에러 2 : 자릿수 입력 오류 ====> " + number.toString());
				return null;
			}
			return number.replaceAll(regEx, "$1-$2-$3"); // 출력 xxx-xxxx-xxxx
		}
	}
	private void menu() {
        System.out.println("=====================회원 관리========================");
        System.out.println("1. 회원정보 조회");
        System.out.println("2. 회원정보 수정");
        System.out.println("3. 회원탈퇴");
        System.out.println("4. 돌아가기");
        System.out.println("======================================================");
        System.out.println();
    }

	public void loginMenu() throws NumberFormatException, IOException {
        while (true) {
            System.out.println();

            switch (Integer.parseInt(br.readLine())) {
            case 1:
                try {
                    register();
                } catch (NoSuchAlgorithmException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break; // 회원가입
            case 2:
                login();
                break; // 로그인
            case 3:
                logout();
                break; // 로그아웃
            case 4: System.out.println("메인메뉴로 돌아갑니다.");
                return;
            } // switch end
        } // while end
    }

    @Override
    public void memberMenu() throws NumberFormatException, IOException {
        while (true) {
            menu();
            switch (Integer.parseInt(br.readLine())) {
            case 1:
                myProfile();
                break; // 회원정보
            case 2:
                editProfile();
                break; // 회원 수정
            case 3:
                removeMember();
                break; // 회원 삭제
            case 4: System.out.println("메인메뉴로 돌아갑니다.");
                return;
            } // switch end
        } // while end
    }
}