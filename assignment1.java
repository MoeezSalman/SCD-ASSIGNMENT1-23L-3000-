import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

class Task {
    int id;
    String title;
    String startime;
    String endtime;
    int[] dependencies;
    int depCount;

    public Task(int id, String title, String start, String end) {
        this.id = id;
        this.title = title;
        this.startime = start;
        this.endtime = end;
        this.dependencies = new int[10];
        this.depCount = 0;
    }

    public void addDependency(int depId) {
        if (depCount < dependencies.length) {
            dependencies[depCount++] = depId;
        }
    }

    public int getDurationHours() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd+HHmm");
        try {
            Date startDate = sdf.parse(startime);
            Date endDate = sdf.parse(endtime);
            long difference = endDate.getTime() - startDate.getTime();
            return (int) (difference / (1000 * 60 * 60));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public String toString() {
        return id + " - " + title + " ( " + startime + " to " + endtime + " )";
    }
}

class Resource {
    String name;
    int[] taskIds;
    int[] completedtaskamount;
    int count;

    public Resource(String name, int maxTasks) {
        this.name = name;
        this.taskIds = new int[maxTasks];
        this.completedtaskamount = new int[maxTasks];
        this.count = 0;
    }

    public void assignTask(int taskId, int percentage) {
        taskIds[count] = taskId;
        completedtaskamount[count] = percentage;
        count++;
    }
}

class Project {
    Task[] tasks;
    Resource[] resources;
    int taskcount;
    int resourcecount;

    public Project(int maxtasks, int maxResources) {
        tasks = new Task[maxtasks];
        resources = new Resource[maxResources];
        taskcount = 0;
        resourcecount = 0;
    }

    public void addTask(Task t) {
        tasks[taskcount++] = t;
    }

    public void addResource(Resource r) {
        resources[resourcecount++] = r;
    }

    public String getcompletionTime() {
        String max = tasks[0].endtime;
        for (int i = 1; i < taskcount; i++) {
            if (tasks[i].endtime.compareTo(max) > 0) {
                max = tasks[i].endtime;
            }
        }
        return max;
    }

    public void findDependencyOverlaps() {
        
        boolean found = false;
        
        for (int i = 0; i < taskcount; i++) {
            Task currentTask = tasks[i];
            
            for (int j = 0; j < currentTask.depCount; j++) {
                int depId = currentTask.dependencies[j];
                Task dependency = findTask(depId);
                
                if (dependency != null) {
                    // Check if current task starts before its dependency ends
                    if (currentTask.startime.compareTo(dependency.endtime) < 0) {
                        // Check if there's actual time overlap (not just touching)
                        if (currentTask.endtime.compareTo(dependency.startime) > 0) {
                            System.out.println("OVERLAP VIOLATION: " + currentTask.title + 
                                             " starts before its dependency " + dependency.title + " is completed");
                            
                            found = true;
                        }
                    }
                }
            }
        }
        
        if (!found) {
            System.out.println("No dependency violations found.");
        }
    }

    private int calculateOverlapHours(Task task1, Task task2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd+HHmm");
        try {
            Date start1 = sdf.parse(task1.startime);
            Date end1 = sdf.parse(task1.endtime);
            Date start2 = sdf.parse(task2.startime);
            Date end2 = sdf.parse(task2.endtime);
            
            long overlapStart = Math.max(start1.getTime(), start2.getTime());
            long overlapEnd = Math.min(end1.getTime(), end2.getTime());
            
            if (overlapStart < overlapEnd) {
                return (int) ((overlapEnd - overlapStart) / (1000 * 60 * 60));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void getTeamForTask(int taskId) {
        System.out.print("Team for Task " + taskId + ": ");
        for (int i = 0; i < resourcecount; i++) {
            for (int j = 0; j < resources[i].count; j++) {
                if (resources[i].taskIds[j] == taskId) {
                    System.out.print(resources[i].name + " ");
                }
            }
        }
        System.out.println();
    }

    public void calculateEffort() {
        for (int i = 0; i < resourcecount; i++) {
            int total = 0;
            Resource r = resources[i];
            for (int j = 0; j < r.count; j++) {
                int taskId = r.taskIds[j];
                int load = r.completedtaskamount[j];
                Task t = findTask(taskId);
                if (t != null) {
                    total += t.getDurationHours() * load / 100;
                }
            }
            System.out.println(r.name + " total effort = " + total + " hours");
        }
    }

    public Task findTask(int id) {
        for (int i = 0; i < taskcount; i++) {
            if (tasks[i].id == id) return tasks[i];
        }
        return null;
    }
}

public class assignment1 {
    public static void main(String[] args) {
        try {
            Project project = new Project(20, 10);
            BufferedReader br1 = new BufferedReader(new FileReader("tasks.txt"));
            String line;
            
            while ((line = br1.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0].trim());
                String title = parts[1].trim();
                String start = parts[2].trim();
                String end = parts[3].trim();
                
                Task task = new Task(id, title, start, end);
                
                // Add dependencies if they exist
                for (int i = 4; i < parts.length; i++) {
                    int depId = Integer.parseInt(parts[i].trim());
                    task.addDependency(depId);
                }
                
                project.addTask(task);
            }
            br1.close();

            BufferedReader br2 = new BufferedReader(new FileReader("resources.txt"));
            while ((line = br2.readLine()) != null) {
                String[] parts = line.split(",");
                Resource r = new Resource(parts[0].trim(), 20);
                for (int i = 1; i < parts.length; i++) {
                    String[] taskInfo = parts[i].split(":");
                    int taskId = Integer.parseInt(taskInfo[0].trim());
                    int percentage = Integer.parseInt(taskInfo[1].trim());
                    r.assignTask(taskId, percentage);
                }
                project.addResource(r);
            }
            br2.close();
            
            // Project completion time output
            System.out.println("Project completion time: " + project.getcompletionTime());
            project.findDependencyOverlaps();
            project.getTeamForTask(3);
            project.calculateEffort();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}