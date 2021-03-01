package ro.mlpn.test.kgreeter;

import ro.mlpn.test.kgreeter.model.UserDetails;

import java.util.List;

class GreetingMessageBuilder {

    public String buildGreetingMessage(UserDetails newUser, List<UserDetails> recentUsers) {
        String messageFirstPart = "Hi " + newUser.getName() + ", welcome to komoot.";
        String messageSecondPart = buildRecentUserMessage(recentUsers);

        return messageFirstPart + messageSecondPart;
    }

    private String buildRecentUserMessage(List<UserDetails> recentUsers) {
        if (recentUsers.isEmpty()) {
            return "";
        }
        return " " + buildUserEnumeration(recentUsers) + " also joined recently.";
    }

    private String buildUserEnumeration(List<UserDetails> recentUsers) {
        switch (recentUsers.size()) {
            case 1:
                return recentUsers.get(0).getName();
            case 2:
                return recentUsers.get(0).getName() + " and " + recentUsers.get(1).getName();
            case 3:
                return recentUsers.get(0).getName() + ", " + recentUsers.get(1).getName() + " and " + recentUsers.get(2).getName();
            default:
                throw new IllegalArgumentException("Unsupported number of entries " + recentUsers.size());
        }
    }
}
