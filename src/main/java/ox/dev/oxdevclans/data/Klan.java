package ox.dev.oxdevclans.data;

import java.util.HashSet;
import java.util.Set;

public class Klan {
    private final String name;
    private final Set<String> leaders;
    private final Set<String> members;

    public Klan(String name, String owner) {
        this.name = name;
        this.leaders = new HashSet<>();
        this.members = new HashSet<>();
        this.leaders.add(owner);
        this.members.add(owner);
    }

    public String getName() {
        return name;
    }

    public Set<String> getLeaders() {
        return new HashSet<>(leaders);
    }

    public Set<String> getMembers() {
        return new HashSet<>(members);
    }

    public boolean addLeader(String leader) {
        return leaders.add(leader) && members.add(leader);
    }

    public boolean removeMember(String member) {
        leaders.remove(member);
        return members.remove(member);
    }

    public boolean addMember(String member) {
        return members.add(member);
    }
}



