import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Schedule {
	private final double studentWeight = Scheduler.studentWeight;
	private final double scheduleWeight = Scheduler.scheduleWeight;
	
	private final HashMap<Integer, Room> rooms;
    private final HashMap<Integer, Teacher> teachers;
    private final HashMap<Integer, Module> modules;
    private final HashMap<Integer, Student> students;
    private final HashMap<Integer, Block> blocks;
    private Class classes[];
    
    private int numClasses = 0;
    private int[] moduleArray;
	private double studentScore = 0;
	
	private int clashes = 0;
	private double scheduleScore = 0;
	private int studentSuccess =0;
	private int studentClashes = 0; //not reach 7
	
    public Schedule() {
        this.rooms = new HashMap<Integer, Room>();
        this.teachers = new HashMap<Integer, Teacher>();
        this.modules = new HashMap<Integer, Module>();
        this.students = new HashMap<Integer, Student>();
        this.blocks = new HashMap<Integer, Block>();
    }
    
    public Schedule(Schedule cloneSchedule) {
        this.rooms = cloneSchedule.getRooms();
        this.teachers = cloneSchedule.getTeachers();
        this.modules = cloneSchedule.getModules();
        this.students = cloneSchedule.getStudents();
        this.blocks = cloneSchedule.getBlocks();
        this.setNumClasses();
        this.generateModuleArray();
    }
    
    public HashMap<Integer, Room> getRooms() {
        return this.rooms;
    }
    
    public Room getRoom(int roomId) {
        if (!this.rooms.containsKey(roomId)) {
        	System.out.println("Rooms doesn't contain key " + roomId); 
        }
        return (Room) this.rooms.get(roomId);
    }
    
    public Room getRandomRoom() {
        Object[] roomsArray = this.rooms.values().toArray();
        Room room = (Room) roomsArray[(int) (roomsArray.length*Math.random())];
        return room;
    }

    
    private HashMap<Integer, Teacher> getTeachers() {
        return this.teachers;
    }
    
    public Teacher getTeacher(int teacherId){
    	return (Teacher) this.teachers.get(teacherId);
    }
    
    private HashMap<Integer, Module> getModules(){
    	return this.modules;
    }
    
    public Module getModule(int moduleId) {
        return (Module) this.modules.get(moduleId);
    }
    
    public int getModuleIdFromModuleArray(int id){
    	return moduleArray[id];
    }
    
    public int[][] getStudentModules(int studentId) {
        Student student = (Student) this.students.get(studentId);
        return student.getPreferredClasses();
    }
    
    private HashMap<Integer, Student> getStudents() {
        return this.students;
    }
    
    public Student getStudent(int studentId){
    	return (Student) this.students.get(studentId);
    }
    
    public Student[] getStudentAsArray(){
    	return (Student[]) (this.students.values().toArray(new Student[this.students.size()]));
    }
    

    private HashMap<Integer, Block> getBlocks(){
    	return this.blocks;
    }
    
    public Block getBlock(int blockId){
    	return (Block) this.blocks.get(blockId);
    }
    
    public Block getRandomBlock(){
    	return (Block) this.blocks.values().toArray()[(int)(blocks.size()*Math.random())];
    }
    
    public void addRoom(int roomId, String roomName, int capacity, boolean isLab) {
        this.rooms.put(roomId, new Room(roomId, roomName, capacity, isLab));
    }
    
    public void addTeachers(int teacherId, String teacherName) {
        this.teachers.put(teacherId, new Teacher(teacherId, teacherName));
    }
    
    public void addModule(int moduleId, String moduleCode, int numOfClass, String module, boolean isLab, int teachersIds[]) {
         this.modules.put(moduleId, new Module(moduleId, moduleCode, numOfClass, module, isLab, teachersIds));
    }
    
    public void addStudent(int studentId, int studentSchoolId, String lastName, String middleName, String firstName, int grade, int preferredClasses[][]) {
    	this.students.put(studentId, new Student(studentId, studentSchoolId, lastName, middleName, firstName, grade, preferredClasses));
    }
    
    public void addBlock(int blockId, String blockTime) {
        this.blocks.put(blockId, new Block(blockId, blockTime));
    }
    
    public void generateModuleArray(){
    	int counter =0;
    	moduleArray = new int[getNumClasses()];
    	for(int i=0;i<modules.size();i++){
    		for(int j=0; j<modules.get(i+1).getNumOfClass(); j++){
    			moduleArray[counter] = 0;
    			counter++;
    		}
    	}
    }
    
    public Class[] getClasses(){
    	return this.classes;
    }
    
    public void setNumClasses() {
    	for(int i=0; i<modules.size(); i++){
    		numClasses += modules.get(i+1).getNumOfClass();
    	}
    }
    
    public int getNumClasses(){
    	return this.numClasses;
    }
    
    public double getScheduleScore(){
    	return scheduleScore;
    }
    
    public double getStudentScore(){
    	return studentScore;
    }
    
    public int getScheduleClash(){
    	return clashes;
    }
    
    public int getStudentClashes(){
    	return studentClashes;
    }
    
    public double[] getScores() throws IOException{
    	double[] dA = new double[4];
    	dA[0] = scheduleScore;
    	dA[1] = studentScore;
    	dA[2] = clashes;
    	dA[3] = studentClashes;
    	return dA;
    }
    
    public void createClasses(Individual individual) {
    	classes = new Class[this.getNumClasses()];
    	int chromosome[] = individual.getChromosome();
    	int cI = 0; //class index
    	//System.out.println("Chromosome length: " + chromosome.length);
		for(int i=0; i<modules.size(); i++){
			//System.out.println(1);
			for(int j=0; j<modules.get(i+1).getNumOfClass(); j++){
				//System.out.println(modules.get(i).getNumOfClass());
				String tempClassName = modules.get(i+1).getModuleCode() + ".0" + (j+1);
				classes[cI] = new Class(cI, tempClassName, modules.get(i+1).getModuleId());
				classes[cI].addClassFromGenes(chromosome[(int)(3*cI)], chromosome[(int)(3*cI+1)], chromosome[(int)(3*cI+2)]);
				cI++;
			}
		}
    }
    

    
    public double calculateScheduleScore() throws IOException{
		scoreClear();
		clashes = checkScheduleClashes(this.classes, modules, rooms);
		int[] tempA = checkStudentClashes(students, classes);
		studentSuccess = tempA[0];
		studentClashes = tempA[1];
    	scheduleScore = scheduleScoreCalc(clashes);
    	studentScore = studentScoreCalc(studentSuccess, studentClashes);
    	return fitnessCalc(scheduleScore, studentScore);
    }
    
	public double calculateFScheduleScore(int gen) throws IOException{
		System.out.println();
		System.out.println();
		System.out.println("----------------------------- Class Schedule Evolution Process Finished --------------------------------");
		System.out.println();
		System.out.println();
		System.out.println("----------------------------- Students' Schedules --------------------------------");
		System.out.println();

		scoreClear();
		Interpretation in = new Interpretation();
		clashes = checkScheduleClashes(this.classes, modules, rooms);

		Object[] sA = students.values().toArray();
		int stdCounter = 1;
		for(Object s: sA){
			in.writeIntToSheet(4, stdCounter, 0, ((Student)s).getStudentId());
			in.writeIntToSheet(4, stdCounter, 1, ((Student)s).getStudentSchoolId());
			in.writeStrToSheet(4, stdCounter, 2, ((Student)s).getFirstName());
			in.writeStrToSheet(4, stdCounter, 3, ((Student)s).getMiddleName());
			in.writeStrToSheet(4, stdCounter, 4, ((Student)s).getLastName());
			in.writeIntToSheet(4, stdCounter, 5, ((Student)s).getGrade());

			int n = ((Student) s).numberFit(classes);
			ArrayList<int[]> ta = ((Student)s).getMaxBlockAL();
			ArrayList<Integer> tPCL = ((Student)s).getMaxPCL();
			System.out.println(((Student) s).getLastName() + " " + ((Student) s).getFirstName() + " Schedule: ");
			if(n == 7){
				in.writeBolToSheet(4, stdCounter, 6, true);
				for(int i=0; i<((Student) s).getPreferredClasses()[0].length; i++){
					try{
						int cellNum = ta.get(0)[i]+6;
						in.writeStrToSheet(4, stdCounter, cellNum, modules.get(((Student) s).getPreferredClasses()[tPCL.get(0)][i]).getModuleCode());
						System.out.println(modules.get(((Student) s).getPreferredClasses()[tPCL.get(0)][i]).getModuleCode() + ": Block " + ta.get(0)[i]);
					}catch(Exception e){
						System.out.println("Error: " + e);
					}
				}
				studentSuccess++;
			}else{
				in.writeBolToSheet(4, stdCounter, 6, false);
				System.out.println("Student's schedule has conflicts");
				studentClashes++;

			}
			stdCounter++;
			System.out.println();
		}
		scheduleScore = scheduleScoreCalc(clashes);
    	studentScore = studentScoreCalc(studentSuccess, studentClashes);
    	
		System.out.println();
		System.out.println();
		System.out.println("----------------------------- Final Evaluation --------------------------------");
		System.out.println();
		System.out.println();
    	System.out.println("Schedule clashes: " + clashes);
		System.out.println("Student clashes: " + studentClashes);
		System.out.println("Schedule score: " + scheduleScore);
		System.out.println("Student score: " + studentScore);
		System.out.println("Actual Score: " + fitnessCalc(scheduleScore, studentScore));		
		System.out.println();
		System.out.println();
		System.out.println("----------------------------- All Classes Schedules --------------------------------");
		System.out.println();
		System.out.println();
		
		return fitnessCalc(scheduleScore, studentScore);
	}
	
	public void scoreClear(){
		studentSuccess = 0;
    	studentClashes = 0;
		studentScore = 0;
		clashes = 0;
		scheduleScore = 0;
    }
    
    public double scheduleScoreCalc(int clashes){
    	double sc = 100.0-(Math.pow(1.8, clashes));
    	if(sc<0){
    		sc = 0;
    	}else if (sc == 99){
    		sc += 1;
    	}
    	return sc;
    }
    
    public double studentScoreCalc(int ss, int sc){
    	double score = (ss/(double)(ss+sc))*100;
    	return score;
    }
    
    public double fitnessCalc(double sc, double stc){
    	double fitness = sc*scheduleWeight+stc*studentWeight;
    	return fitness;
    }
    
    public int checkScheduleClashes(Class[] dummyClass, HashMap<Integer,Module> mods, HashMap<Integer,Room> rms){
    	int cl = 0;
    	for (Class classA : dummyClass) {
    		for (Class classB : dummyClass) {
                if (classA.getRoomId() == classB.getRoomId() && classA.getBlockId() == classB.getBlockId() && classA.getClassId() != classB.getClassId()){
                	cl++;
                    break; 
                }
            }
	    	for (Class classB : dummyClass) {
	    		if (classA.getTeacherId() == classB.getTeacherId() && classA.getBlockId() ==classB.getBlockId() && classA.getClassId() != classB.getClassId()) {
	    	       cl++;
	    	       break;
	    		}
	    	}
	    	if(mods.get(classA.getModuleId()).getIsLab()){
	    		if(!rms.get(classA.getRoomId()).getIsLab()){
	    			cl++;
	    		}
	    	}
    	}
    	return cl;
    }
    
    public int[] checkStudentClashes(HashMap<Integer,Student> stuHM, Class[] classArray){
    	int[] sCA = new int[2];
    	sCA[0] = 0;
    	sCA[1] =0;
    	Object[] sA = stuHM.values().toArray();
    	for(Object s: sA){
    		int n = ((Student)s).numberFit(classArray);
			if(n == 7){
				sCA[0]++;
    		}else{
    			sCA[1]++;
    		}
    	}
    	return sCA;
    }
	
}

