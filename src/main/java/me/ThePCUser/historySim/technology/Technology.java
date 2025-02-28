package me.ThePCUser.historySim.technology;

/**
 * Represents a technology that players can research
 */
public class Technology {
    private final String id;
    private final String name;
    private final String description;
    private final int researchPoints;
    private final String prerequisite;

    public Technology(String id, String name, String description, int researchPoints) {
        this(id, name, description, researchPoints, null);
    }

    public Technology(String id, String name, String description, int researchPoints, String prerequisite) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.researchPoints = researchPoints;
        this.prerequisite = prerequisite;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getResearchPoints() {
        return researchPoints;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public boolean hasPrerequisite() {
        return prerequisite != null && !prerequisite.isEmpty();
    }
}