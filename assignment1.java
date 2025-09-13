import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
class Task{                             // For the calculation of the number of tasks

int id;
String title;
String startime;
String endtime;



public Task(int id, String title , String start, String end){

    this.id=id;
    this.title=title;
    this.startime=start;
    this.endtime=end;
   

}


public int getDurationHours() {
    // Parse the date strings properly
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd+HHmm");
    
    try {
        Date startDate = sdf.parse(startime);
        Date endDate = sdf.parse(endtime);
        
        long difference = endDate.getTime() - startDate.getTime();
        return (int) (difference / (1000 * 60 * 60)); // Convert milliseconds to hours
    } catch (ParseException e) {
        e.printStackTrace();
        return 0;
    }
}



@Override
public String toString(){
    return id + " - "+title+" ( "+startime+ " to "+endtime+" )";
}

}



class Resource{                          // For adding the resources from the resource.txt file
    String name;
    int[] taskIds;
    int[] completedtaskamount;
    int count;

    public Resource(String name, int maxTasks){
        this.name=name;
        this.taskIds=new int[maxTasks];
        this.completedtaskamount= new int[maxTasks];
        this.count=0;
    }

    public void assignTask(int taskId, int percentage){
        taskIds[count]=taskId;
        completedtaskamount[count]=percentage;
        count++;
    }
}

class Project{                          //For all the add resource,getting completion time and functionalities

    Task[] tasks;
    Resource[] resources;
    int taskcount;
    int resourcecount;

    public Project(int maxtasks, int maxResources){
        tasks=new Task[maxtasks];
        resources= new Resource[maxResources];
        taskcount=0;
        resourcecount=0;


    }

    public void addTask(Task t){
        tasks[taskcount++]=t;
    }

    public void addResource(Resource r){
        resources[resourcecount++]=r;
    }

    public String getcompletionTime(){
        String max=tasks[0].endtime;
        for(int i=1;i<taskcount;i++){
            if(tasks[i].endtime.compareTo(max)>0){
                max=tasks[i].endtime;
            }
        }
        return max;
    }

    public void findoverlaps(){                             //Checking overlaps
        for(int i=0;i<taskcount;i++){
            for(int j=i+1;j<taskcount;j++){
                Task t1=tasks[i], t2=tasks[j];
                if (t1.endtime.compareTo(t2.startime) > 0 && t1.startime.compareTo(t2.endtime) < 0) {
                    System.out.println("Overlap: " + t1.title + " & " + t2.title);
                }
            }
        }
    }


    public void getTeamForTask(int taskId) {                //Finding the name of the assigned task employee
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

 


    public void calculateEffort() {                       //Getting effort of all the members
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




public class assignment1{                           //Main branch
    public static void main(String[] args){

        try{
            Project project= new Project(20, 10);
            BufferedReader br1 = new BufferedReader(new FileReader("tasks.txt"));
            String line;
            while ((line = br1.readLine()) != null) {

                String[] parts=line.split(",");
                int id = Integer.parseInt(parts[0].trim());
                String title = parts[1].trim();
                String start = parts[2].trim();
                String end = parts[3].trim();
                

                

                 project.addTask(new Task(id, title, start, end));

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
             System.out.println("Project completion time: " + project.getcompletionTime());
            project.findoverlaps();
            project.getTeamForTask(3);
            project.calculateEffort();
        }
        catch(Exception e){
            e.printStackTrace();
        }



    }
}

