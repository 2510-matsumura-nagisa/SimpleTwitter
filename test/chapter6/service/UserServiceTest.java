package chapter6.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import chapter6.beans.User;
import chapter6.utils.CipherUtil;
import chapter6.utils.DBUtil;

// 1. テストクラス名は任意のものに変更してください。
// 2. L.23~86は雛形として使用してください。
// 3．L.44のファイル名は各自作成したファイル名に書き換えてください。
public class UserServiceTest {

	private File file;
	private int usersTestDataSize;

	@Before
	public void setUp() throws Exception {

		IDatabaseConnection connection = null;
		try {
			Connection conn = DBUtil.getConnection();
			connection = new DatabaseConnection(conn);

			//(2)現状のバックアップを取得
			QueryDataSet partialDataSet = new QueryDataSet(connection);
			partialDataSet.addTable("users");

			file = File.createTempFile("temp", ".xml");
			FlatXmlDataSet.write(partialDataSet,
					new FileOutputStream(file));

			PreparedStatement ps = null;
			ps = conn.prepareStatement("TRUNCATE TABLE users");
			ps.executeUpdate();

			//(3)テストデータを投入する
			IDataSet dataSetMessage = new FlatXmlDataSet(new File("users_data_init.xml"));
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSetMessage);

			IDataSet expectedDataset = new FlatXmlDataSet(new File("users_data_init.xml"));

			ITable expectedTable = expectedDataset.getTable("users");
			this.usersTestDataSize = expectedTable.getRowCount();

			DBUtil.commit(conn);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		IDatabaseConnection connection = null;
		try {
			Connection conn = DBUtil.getConnection();
			connection = new DatabaseConnection(conn);

			IDataSet dataSet = new FlatXmlDataSet(file);
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

			DBUtil.commit(conn);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			//一時ファイルの削除
			if (file != null) {
				file.delete();
			}
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
			}

		}
	}

	@Test
	public void testInsertUser() throws Exception {
		//インスタンスを生成
		List<User> insertUserList = new ArrayList<>();
		User user001 = new User();
		user001.setAccount("test001insert");
		user001.setName("テスト001insert");
		user001.setEmail("test001@insert.com");
		user001.setPassword("hoge001insert");
		user001.setDescription("テスト001insert");
		insertUserList.add(user001);

		User user002 = new User();
		user002.setAccount("test002insert");
		user002.setName("テスト002insert");
		user002.setEmail("test002@insert.com");
		user002.setPassword("hoge002insert");
		user002.setDescription("テスト002insert");
		insertUserList.add(user002);

		User user003 = new User();
		user003.setAccount("test003insert");
		user003.setName("テスト003insert");
		user003.setEmail("test003@insert.com");
		user003.setPassword("hoge003insert");
		user003.setDescription("テスト003insert");
		insertUserList.add(user003);

		UserService userService = new UserService();

		//データの登録
		for(int i = 0; i < insertUserList.size(); i++) {
			userService.insert(insertUserList.get(i));
		}

		//データ
		IDatabaseConnection connection = null;
		try {
			Connection conn = DBUtil.getConnection();
			connection = new DatabaseConnection(conn);
			//メソッド実行した実際のテーブル
			IDataSet databaseDataSet = connection.createDataSet();
			ITable actualTable = databaseDataSet.getTable("users");
			// 期待されるべきテーブルデータを表すITableインスタンスを取得
			IDataSet expectedDataSet = new FlatXmlDataSet(new
					File("users_data_insert.xml"));
			ITable expectedTable = expectedDataSet.getTable("users");
			//期待されるITableと実際のITableの比較
			//id、created_date、updated_date、passwordを除いたデータを確認
			Assertion.assertEqualsIgnoreCols(actualTable, expectedTable, new
					String[] {"id", "created_date", "updated_date", "password"});

			for (int i = 0; i < insertUserList.size(); i++) {
				assertEquals(CipherUtil.encrypt(
						(String)expectedTable.getValue(i +
								this.usersTestDataSize, "password")),
						(String)actualTable.getValue(i +
								this.usersTestDataSize, "password"));
			}
		} finally {
				if (connection != null) {
					connection.close();
				}
		}
	}

	@Test
	public void testUserUpdate() throws Exception {

		//更新用のデータ
		List<User> updateUserList = new ArrayList<>();
		User user001 = new User();
		user001.setAccount("test001update");
		user001.setName("テスト001update");
		user001.setEmail("test001@update.com");
		user001.setPassword("hoge001update");
		user001.setDescription("テスト001update");
		updateUserList.add(user001);

		User user002 = new User();
		user002.setAccount("test002update");
		user002.setName("テスト002update");
		user002.setEmail("test002@update.com");
		user002.setPassword("");
		user002.setDescription("テスト002update");
		updateUserList.add(user002);

		User user003 = new User();
		user003.setAccount("test003update");
		user003.setName("テスト003update");
		user003.setEmail("test003@update.com");
		user003.setPassword("hoge003update");
		user003.setDescription("テスト003update");
		updateUserList.add(user003);

		//更新
		UserService userService = new UserService();
		for(int i = 0; i < updateUserList.size(); i++) {
			updateUserList.get(i).setId(i + 1);
			userService.update(updateUserList.get(i));
		}

		IDatabaseConnection connection = null;
		try {
			Connection conn = DBUtil.getConnection();
			connection = new DatabaseConnection(conn);
			//メソッド実行した実際のテーブル
			IDataSet databaseDataSet = connection.createDataSet();
			ITable actualTable = databaseDataSet.getTable("users");
			//期待されるべきテーブルデータを表すITableインスタンスを取得
			IDataSet expectedDataSet = new FlatXmlDataSet(new
					File("users_data_update.xml"));
			ITable expectedTable = expectedDataSet.getTable("users");
			//期待されるITableと実際のITableの比較
			//id、created_date、updated_date、passwordを除いたデータを確認
			Assertion.assertEqualsIgnoreCols(actualTable, expectedTable, new
					String[] {"id", "created_date", "updated_date", "password"});

			//passwordの確認
			for (int i = 0; i < updateUserList.size(); i++) {
			    if(!StringUtils.isBlank(updateUserList.get(i).getPassword())){
			        assertEquals(CipherUtil.encrypt((String)
	                         expectedTable.getValue(i, "password")), (String)
	                         actualTable.getValue(i, "password"));
				} else {
					assertEquals(expectedTable.getValue(i, "password"),
							(String)actualTable.getValue(i, "password"));
				}
			}
		} finally {
			if (connection != null) {
				connection.close();
	             }
		}
	}

	@Test
	public void testUserSelect() throws Exception {

		//Userのリスト
		List<User> selectUsersList = new ArrayList<>();

		//インスタンス生成
		UserService userService = new UserService();

		//テストデータの文だけfor文を回して、ユーザを取得する
		for(int i = 0; i < this.usersTestDataSize; i++) {
			selectUsersList.add(userService.select(i + 1));
		}

		//件数
		assertEquals(3, selectUsersList.size());

		User user001 = selectUsersList.get(0);
		assertEquals("id=1", "id=" + user001.getId());
		assertEquals("account=test001", "account=" + user001.getAccount());
		assertEquals("name=テスト001", "name=" + user001.getName());
		assertEquals("email=test001@com", "email=" + user001.getEmail());
		assertEquals("password=hoge001", "password=" + user001.getPassword());
		assertEquals("description=テスト001", "description=" +
				user001.getDescription());

		User user002 = selectUsersList.get(1);
		assertEquals("id=2", "id=" + user002.getId());
		assertEquals("account=test002", "account=" + user002.getAccount());
		assertEquals("name=テスト002", "name=" + user002.getName());
		assertEquals("email=test002@com", "email=" + user002.getEmail());
		assertEquals("password=hoge002", "password=" + user002.getPassword());
		assertEquals("description=テスト002", "description=" +
				user002.getDescription());

		User user003 = selectUsersList.get(2);
		assertEquals("id=3", "id=" + user003.getId());
		assertEquals("account=test003", "account=" + user003.getAccount());
		assertEquals("name=テスト003", "name=" + user003.getName());
		assertEquals("email=test003@com", "email=" + user003.getEmail());
		assertEquals("password=hoge003", "password=" + user003.getPassword());
		assertEquals("description=テスト003", "description=" +
				user003.getDescription());
	}
}
