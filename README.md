# SPRING COMMON USING LIB

### Main functions supporting
- Create criteria from params on requestm customize for normal and simple query
This function base on https://www.baeldung.com/rest-api-query-search-language-more-operations
Function query through specification supports One to one, one to many relation in model

- Automatically copy value from request object into model object
This function will ignore null value, this function will automatically create object based on type of relationship
  
Database interceptor instruction setup:
- Extending class BaseEntityAuditLogInterceptor and implement your own method writeLog()
- Model needs to extend com.conght.common.BaseModel to start logging. This class will use variable
isCreate in BaseModel class to identify create or update function. 

### Instruction 
1. Automatically copy value from request object into model object
- Step 1: Create request class
```Java
public class ProjectRequest {
 @NotNull
 @Size(min = 1, max = 255)
 private String name;

 private List<Long> taskManagerIds;

 @NotNull(message = "Invalid project manager")
 private Long projectManagerId;
}
```
- Step 2: Create model class
```Java
public class Project {
    public Project(long id) {this.id = id;}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name="pm_admin_seq")
    private User projectManager;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_rel_admin_project",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "admin_id"))
    @JsonManagedReference
    private List<User> taskManagers;
    
    private String name;
}
```
- Step 3: In service file, we just need to call method from Utils in lib, this method will copy value automatically with primitive or java original type into corresponding in 
entity object. With relationship field, it requires declaring field name follow the rule:
  + OneToOne or OneToMany relationship: field-name-in-request-class = field-name-in-entity-class + "Id"
  + ManyToMany: field-name-in-request-class = field-name-in-entity-class + "Ids"
  + Primary key always must be "id"
```Java
import com.conght.common.requestcriteria.util.RequestPropertiesUtil;

@Service
public class ProjectServiceImpl implements ProjectService {
        @Override
    public Project update(long id, ProjectRequest projectRequest) {
        Optional<Project> result = this.show(id);
        if (result.isPresent()) {
            Project project = result.get();
            RequestPropertiesUtil.copyNonNullProperties(projectRequest, project);
            return this.projectRepository.save(project);
        }
        throw new ItemNotFound("Project Not Found Id:" + id);
    }
}
```