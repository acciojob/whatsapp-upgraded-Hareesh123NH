package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most once group
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 1;
        this.messageId = 1;
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        //your code here
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        // The list contains at least 2 users where the first user is the admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group customGroupCount". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // If group is successfully created, return group.
        //your code here
        if (users.size()==2){
            Group g=new Group(users.get(1).getName(),users.size());
            adminMap.put(g,users.get(0));
            groupUserMap.put(g,users);
            return g;
        }
        else{
            String gname="Group "+customGroupCount;
            customGroupCount++;
            Group g=new Group(gname,users.size());
            adminMap.put(g,users.get(0));
            groupUserMap.put(g,users);
            return g;
        }
    }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        //your code here
        Message msg=new Message(messageId,content);
        messageId++;
        return msg.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        //your code here
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        List<User> users=new ArrayList<>(groupUserMap.get(group));
        if(!users.contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        senderMap.put(message,sender);
        if(groupMessageMap.containsKey(group)){
            groupMessageMap.get(group).add(message);
            return groupMessageMap.get(group).size();
        }
        else{
            List<Message> msg=new ArrayList<>();
            msg.add(message);
            groupMessageMap.put(group,msg);
            return groupMessageMap.get(group).size();
        }
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".

        //your code here
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        if(!adminMap.get(group).getMobile().equals(approver.getMobile())){
            throw new Exception("Approver does not have rights");
        }
        if(!groupUserMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        adminMap.put(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        //your code here
        if(adminMap.containsValue(user)){
            throw new Exception("Cannot remove admin");
        }
        userMobile.remove(user.getMobile());
        for(Group gp:groupUserMap.keySet()){
            if(groupUserMap.get(gp).contains(user)){
                groupUserMap.get(gp).remove(user);
                for(Message msg:groupMessageMap.get(gp)){
                    if(senderMap.get(msg).equals(user)){
                        senderMap.remove(msg);
                        groupMessageMap.get(gp).remove(msg);
                    }
                }
                return groupUserMap.get(gp).size()+senderMap.size()+groupMessageMap.get(gp).size();
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        //your code here
        List<Message> msg=new ArrayList<>();
        for(Message m:senderMap.keySet()){
            if((m.getTimestamp().before(end) && m.getTimestamp().after(start)) || m.getTimestamp().equals(start) || m.getTimestamp().equals(end)){
                msg.add(m);
            }
        }
        msg.sort((m1,m2)->m1.getTimestamp().compareTo(m2.getTimestamp()));
        if(K>0 && K<=msg.size()){
            return msg.get(K-1).getContent();
        }
        throw new Exception("K is greater than the number of messages");
    }
}