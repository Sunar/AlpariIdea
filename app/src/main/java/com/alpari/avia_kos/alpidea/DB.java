package com.alpari.avia_kos.alpidea;

import android.content.res.Resources;
import android.util.Log;

import com.alpari.avia_kos.alpidea.models.Idea;
import com.alpari.avia_kos.alpidea.models.IdeaStatus;
import com.alpari.avia_kos.alpidea.models.Message;
import com.alpari.avia_kos.alpidea.models.Prize;
import com.alpari.avia_kos.alpidea.models.User;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Avia-Kos on 05.05.16.
 */
public class DB {
    public final static int ROBOT_ID = 1;
    public final static int ANALITIK_ID = 2;
    private Connection connection = null;
    private static DB ourInstance = new DB();

    public static DB getInstance() {
        return ourInstance;
    }

    private DB() {
    }

    {
        try {
            //System.out.println("Getting a database connection");
            Class.forName("com.mysql.jdbc.Driver");
            Resources res = App.getContext().getResources();
            connection = DriverManager.getConnection(res.getString(R.string.db_address), res.getString(R.string.db_login), res.getString(R.string.db_password));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException{
        Resources res = App.getContext().getResources();
        if(connection == null){
            connection = DriverManager.getConnection(res.getString(R.string.db_address), res.getString(R.string.db_login), res.getString(R.string.db_password));
        }

        if(connection == null)
            throw new SQLException();

        if(connection.isClosed()){
            connection = DriverManager.getConnection(res.getString(R.string.db_address), res.getString(R.string.db_login), res.getString(R.string.db_password));
        }

        return connection;
    }

    public int getClientVersionCode() throws SQLException{
        int version;
        connection = getConnection();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select version from client_versions where client_name = 'android'");
        if(rs.next()){
            return rs.getInt("version");
        }
        return 0;
    }

    private String getRobotEmail() throws SQLException{
        connection = getConnection();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select email from users where id = " + ROBOT_ID);
        if(rs.next())
            return rs.getString("email");
        return null;
    }
    //attempt to login in system
    public boolean logIn(String login, String password) throws SQLException{
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select * from users where login = ? and password = ?");
        ps.setString(1, login);
        ps.setString(2, md5hash(password));
        ResultSet rs = ps.executeQuery();
        try{
            if(rs.next()){
                User user = User.getInstance();
                user.setLogin(login);
                user.setName(rs.getString("full_name"));
                user.setId(rs.getInt("id"));
                user.setTelephone(rs.getString("telephone"));
                user.setEmail(rs.getString("email"));
                attemptLoginAsExpert();
                return true;
            }
            else return false;
        } finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
    }

    private void attemptLoginAsExpert() throws SQLException{
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select * from experts where user_id = ?");
        ps.setInt(1, User.getInstance().getId());
        ResultSet rs = ps.executeQuery();
        try{
            while (rs.next()){
                if(rs.getInt("idea_type_id") != 0)
                    User.getInstance().setExpert(true);
                else
                    User.getInstance().setRuk(true);
            }
        } finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
    }

    //check is login exist in system
    public boolean loginIsVacant(String login) throws SQLException{
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select * from users where login = ?");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();
        try {
            if(rs.next())
                return false;
            else return true;
        }
        finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
    }

    //attempt to register user
    public boolean register(String login, String password, String fio, String telephone) throws SQLException{
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("insert into users (login, password, creation_date, full_name, telephone) values (?,?, NOW(), ?, ?)");
        ps.setString(1, login);
        ps.setString(2, md5hash(password));
        ps.setString(3, fio);
        ps.setString(4, telephone);
        try {
            if(!ps.execute())
                return true;
            return false;
        } finally {
            ps.close();
            connection.close();
        }
    }

    public ArrayList<String> getRuks() throws SQLException {
        ArrayList<String> ruks = new ArrayList<>();
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select full_name from users where id IN (select user_id from experts where idea_type_id = 0)");
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()){
                ruks.add(rs.getString("full_name"));
            }
        }
        finally {
            connection.close();
        }
        return ruks;
    }

    //get list of prizes
    public ArrayList<Prize> getPrizes() throws SQLException{
        ArrayList<Prize> prizes = new ArrayList<>();
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select * from prizes where actual = ?");
        ps.setInt(1, 1);
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()){
                prizes.add(new Prize(rs.getString("name"), rs.getString("description"), rs.getInt("point"), rs.getString("image")));
            }
        }
        finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
        return prizes;
    }

    public Boolean buyPrize(Prize prize) throws SQLException{
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("insert into points_out (date, prize_id, user_id, point) values (now(), (select id from prizes where name = ?), ?, ?)");
        ps.setString(1, prize.getName());
        ps.setInt(2, User.getInstance().getId());
        ps.setInt(3, prize.getPoint());

        try {
            if(ps.executeUpdate() > 0) {
                sendMessageAboutPrize(prize);
                return true;
            }
            return false;
        } finally {
            ps.close();
            connection.close();
        }
    }

    //get list of my ideas
    public ArrayList<Idea> getMyIdeas() throws SQLException{
        ArrayList<Idea> ideas = new ArrayList<>();
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select i.content, i.resources, i.goal, i.realization_time, i.ruk_decision, st.id, st.status, st.point, st.message_template " +
                "from ideas i inner join idea_statuses st on i.idea_status_id = st.id where user_id = ? order by date desc");
        ps.setInt(1, User.getInstance().getId());
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()){
                ideas.add(new Idea(new IdeaStatus(rs.getInt("id"),rs.getString("status"), rs.getInt("point"), rs.getString("message_template")), rs.getString("content"), rs.getString("resources"), rs.getString("goal"), rs.getString("realization_time"), rs.getString("ruk_decision")));
            }
        }
        finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
        return ideas;
    }

    //get list of ideas for expert
    public ArrayList<Idea> getIdeasForExpert() throws SQLException{
        ArrayList<Idea> ideas = new ArrayList<>();
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select i.content, i.resources, i.goal, i.realization_time, i.ruk_decision, st.id, st.status, st.point, st.message_template from ideas i inner join idea_statuses st on i.idea_status_id = st.id where (i.idea_type_id IN " +
                "(select idea_type_id from experts where user_id = ?) and i.expert_user_id is null) or i.expert_user_id = ? or i.expert2_user_id = ? order by i.date desc");
        ps.setInt(1, User.getInstance().getId());
        ps.setInt(2, User.getInstance().getId());
        ps.setInt(3, User.getInstance().getId());
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()){
                ideas.add(new Idea(new IdeaStatus(rs.getInt("id"),rs.getString("status"), rs.getInt("point"), rs.getString("message_template")), rs.getString("content"), rs.getString("resources"), rs.getString("goal"), rs.getString("realization_time"), rs.getString("ruk_decision")));
            }
        }
        finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
        return ideas;
    }

    public ArrayList<Idea> getIdeasForAnalitik() throws SQLException{
        ArrayList<Idea> ideas = new ArrayList<>();
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select i.content, i.resources, i.goal, i.realization_time, i.ruk_decision, st.id, st.status, st.point, st.message_template from ideas i inner join idea_statuses st on i.idea_status_id = st.id order by i.date desc");
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()){
                ideas.add(new Idea(new IdeaStatus(rs.getInt("id"),rs.getString("status"), rs.getInt("point"), rs.getString("message_template")), rs.getString("content"), rs.getString("resources"), rs.getString("goal"), rs.getString("realization_time"), rs.getString("ruk_decision")));
            }
        }
        finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
        return ideas;
    }

    //get idea types
    public ArrayList<String> getIdeaTypes() throws SQLException{
        ArrayList<String> types = new ArrayList<>();
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select * from idea_types");
        ResultSet rs = ps.executeQuery();
        try {
            while(rs.next()){
                types.add(rs.getString("type"));
            }
        }
        finally {
            ps.close();
            connection.close();
        }
        return types;
    }

    //send idea
    public boolean sendIdea(String login, String type, String content, String resources, String goal, String time) throws SQLException{
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("insert into ideas(date, user_id, idea_type_id, content, resources, goal, realization_time, idea_status_id, expert_user_id) " +
                "SELECT now(), ?, tb.idea_type_id, ?, ?, ?, ?, 1, tb.user_id from (SELECT e.user_id, ifnull(i.rows, 0) cnt, e.idea_type_id FROM experts e " +
                "LEFT OUTER JOIN (SELECT expert_user_id, COUNT(*) as rows FROM ideas GROUP BY expert_user_id) i on i.expert_user_id = e.user_id) tb WHERE tb.idea_type_id = (select id from idea_types where type = ?) and cnt = (SELECT min(ifnull(i.rows, 0)) " +
                "FROM experts e LEFT OUTER JOIN (SELECT expert_user_id, COUNT(*) as rows FROM ideas GROUP BY expert_user_id) i on i.expert_user_id = e.user_id WHERE e.idea_type_id = (select id from idea_types where type = ?)) limit 1");
        ps.setInt(1, User.getInstance().getId());
        ps.setString(2, content);
        ps.setString(3, resources);
        ps.setString(4, goal);
        ps.setString(5, time);
        ps.setString(6, type);
        ps.setString(7, type);
        //Отправляем сообщение на почту экспертной группе о новой идее
        boolean result = false;
        Statement st = connection.createStatement();

        try {
            result = ps.executeUpdate() > 0;
            ResultSet rs = st.executeQuery("select email from users where id = (select expert_user_id from ideas where content = '" + content + "')");
            if(rs.next())
                sendEmail(rs.getString("email"), "Есть идея", "Пришла идея на проверку!\n" + content + "\nПроверьте личный кабинет для подробностей");
            rs.close();
            return result;
        } finally {
            ps.close();
            st.close();
            connection.close();
        }
    }

    //calc my points
    public int getMyPoints() throws SQLException {
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select in_total - out_total as total from ( select ifnull(A.user_id, ?) as user_id, ifnull( sum(A.point), 0 ) as in_total " +
                "from points_in A where A.user_id = ?) a " +
                "inner join ( select ifnull(user_id, ?) as user_id, ifnull( sum(B.point), 0 ) as out_total from points_out B ) b on a.user_id = b.user_id");
        int id = User.getInstance().getId();
        ps.setInt(1, id);
        ps.setInt(2, id);
        ps.setInt(3, id);
        ResultSet rs = ps.executeQuery();

        try {
            if(rs.next())
                return rs.getInt("total");
            else
                return 0;
        } finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
    }

    public void expertOpenedIdea(Idea idea) throws SQLException{

        connection = getConnection();

        if(idea.getStatus().getId() > 1) return;

        PreparedStatement ps = connection.prepareStatement("update ideas set idea_status_id = 2, expert_user_id = ifnull(?, expert_user_id) " +
                "where content = ?");
        ps.setInt(1, User.getInstance().getId());
        ps.setString(2, idea.getContent());
        ps.execute();
        PreparedStatement ps2 = connection.prepareStatement("insert ignore into points_in (date, user_id, idea_id, point, idea_status_id) " +
                "select now(), i.user_id, i.id, st.point, 2 from ideas i inner join idea_statuses st on st.id = 2 where i.content = ?");
        ps2.setString(1, idea.getContent());
        ps2.execute();

        idea.setStatus(IdeaStatus.getStatuses().get(1));

        sendAutoMessage(idea);

        connection.close();
    }

    public void expertPointedIdea(Idea idea, int statusId, String rukName) throws SQLException{
        connection = getConnection();
        String setRuk = "";
        if(rukName != null) {
            setRuk = ", expert2_user_id = (select id from users where full_name = '" + rukName + "')";

            //Отправляем сообщение на почту руководителю
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("select email from users where full_name = '" + rukName + "'");
            if(rs.next()){
                sendEmail(rs.getString("email"), "Есть идея", "Пришла идея на проверку!\n" + idea.getContent() + "\nПроверьте личный кабинет для подробностей");
            }
        }
        String updateIdeaString = "update ideas set idea_status_id = ?" + setRuk + " where content = ?";
        PreparedStatement ps = connection.prepareStatement(updateIdeaString);
        ps.setInt(1, statusId);
        ps.setString(2, idea.getContent());
        ps.execute();
        if(IdeaStatus.getStatuses().get(statusId - 1).getPoint() > 0){
            PreparedStatement ps2 = connection.prepareStatement("insert ignore into points_in (date, user_id, idea_id, point, idea_status_id) " +
                    "select now(), i.user_id, i.id, st.point, st.id from ideas i inner join idea_statuses st on st.id = ? where i.content = ?");
            ps2.setInt(1, statusId);
            ps2.setString(2, idea.getContent());
            ps2.execute();
        }
        idea.setStatus(IdeaStatus.getStatuses().get(statusId - 1));
        sendAutoMessage(idea);

        connection.close();
    }

    public void sendRukDecision(int newStatusId, String decision, Idea idea) throws SQLException {
        connection = getConnection();
        String updateIdeaString = "update ideas set idea_status_id = ?, ruk_decision = ? where content = ?";
        PreparedStatement ps = connection.prepareStatement(updateIdeaString);
        ps.setInt(1, newStatusId);
        ps.setString(2, decision);
        ps.setString(3, idea.getContent());
        ps.execute();
        idea.setStatus(IdeaStatus.getStatuses().get(newStatusId - 1));
        sendAutoMessage(idea);
        connection.close();
    }

    public void getListOfStatuses() throws SQLException{

        connection = getConnection();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("select * from idea_statuses");
        while (rs.next()){
            IdeaStatus.pushStatus(new IdeaStatus(rs.getInt("id"),rs.getString("status"), rs.getInt("point"), rs.getString("message_template")));
        }
    }

    public ArrayList<Message> getMessages() throws SQLException {
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("select m.id, m.content, m.date, m.read_this, u.login from_login, (select login from users where id = m.to_id) to_login from messages m inner join users u on u.id = m.from_id where to_id = ? union " +
                "select m.id, m.content, m.date, m.read_this, (select login from users where id = m.from_id) from_login, u.login to_login from messages m inner join users u on u.id = m.to_id where from_id = ? order by date desc");
        ps.setInt(1, User.getInstance().getId());
        ps.setInt(2, User.getInstance().getId());
        ResultSet rs = ps.executeQuery();
        ArrayList<Message> messages = new ArrayList<>();
        try {
            while (rs.next()) {
                messages.add(new Message(rs.getInt("id"), rs.getString("content"), rs.getString("from_login"), rs.getString("to_login"), rs.getString("date"), rs.getBoolean("read_this")));
            }
            return messages;
        }
        finally {
            ps.close();
            if(rs != null) rs.close();
            connection.close();
        }
    }

    public void readMessage(Message message) throws SQLException {
        if(message.isRead()) return;
        connection = getConnection();
        Statement st = connection.createStatement();

        try {
            st.execute("update messages set read_this = 1 where id = " + message.getId());
        } finally {
            st.close();
            connection.close();
        }

    }

    public boolean sendMessage(int from_id, String to_login, String message) throws SQLException{
        connection = getConnection();
        PreparedStatement ps = connection.prepareStatement("insert into messages (date, content, to_id, from_id, read_this) select now(), ?, id, ?, 0 from users where login = ?");
        ps.setString(1, message);
        ps.setInt(2, from_id);
        ps.setString(3, to_login);
        return ps.executeUpdate() > 0;

    }

    private void sendAutoMessage(Idea idea) throws SQLException {

        if(idea.getStatus().getMessageTemplate() == null) return;

        connection = getConnection();
        String message = idea.getStatus().getMessageTemplate();
        PreparedStatement ps = connection.prepareStatement("select user_id, expert_user_id from ideas where content = ?");

        ps.setString(1, idea.getContent());
        int id_to = 0;
        int id_from = 0;
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            id_to = rs.getInt("user_id");
            //id_from = rs.getInt("expert_user_id");
            id_from = 1;
        }

        PreparedStatement ps2 = connection.prepareStatement("insert into messages(date, content, to_id, from_id, read_this) values(now(), ?, ?, ?, 0)");
        try {
                if(id_to == 0) return;
                ps2.setString(1, message);
                ps2.setInt(2, id_to);
                ps2.setInt(3, id_from);
                ps2.execute();
        }
        finally {
            ps.close();
            if(ps2 != null) ps2.close();
            if(rs != null) rs.close();
            connection.close();
        }
    }

    private void sendMessageAboutPrize(Prize prize) throws SQLException {
        connection = getConnection();
        String message = User.getInstance().getLogin() + " (" + User.getInstance().getName() + ") " + " приобрел(а) подарок \"" + prize.getName() + "\"! Тел. для связи: " +
                (User.getInstance().getTelephone() == null? "неизвестен" : User.getInstance().getTelephone());
        String message2 = "Поздравляем с приобретением! С вами свяжутся в ближайшее время";
        PreparedStatement ps = connection.prepareStatement("insert into messages(date, content, to_id, from_id, read_this) values(now(), ?, ?, ?, 0)");
        Statement st = connection.createStatement();
        try {
            ps.setString(1, message);
            ps.setInt(2, ANALITIK_ID);
            ps.setInt(3, ROBOT_ID);
            ps.execute();

            ps.setString(1, message2);
            ps.setInt(2, User.getInstance().getId());
            ps.execute();

            ResultSet rs = st.executeQuery("select email from users where id = " + ANALITIK_ID);
            if(rs.next())
                sendEmail(rs.getString("email"), "Есть идея: пользователь купил приз", message);
            rs.close();
        }
        finally {
            ps.close();
            st.close();
            connection.close();
        }
    }

    private void sendEmail(String email, String subject, String message){
        try {
            if(email == null) return;
            Resources res = App.getContext().getResources();
            GMailSender sender = new GMailSender(res.getString(R.string.robot_email), res.getString(R.string.robot_password));
            sender.sendMail(subject,
                    message,
                    res.getString(R.string.robot_email),
                    email);
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }
    
    //get hash of string
    private static String md5hash(String st) {
        MessageDigest messageDigest = null;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            // тут можно обработать ошибку
            // возникает она если в передаваемый алгоритм в getInstance(,,,) не существует
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while( md5Hex.length() < 32 ){
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }
}
