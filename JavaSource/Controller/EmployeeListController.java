package Controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import Model.Employee;

@Named("employeeListController")
@ViewScoped
public class EmployeeListController implements Serializable {

    @Inject
    private DatabaseController database;

    private List<Employee> employees;

    private Employee editEmployee;

    @PostConstruct
    public void init() {
        editEmployee = null;
        employees = database.getEmployees();
    }

    public void addEmployee(String empNo, String firstName, String lastName,
            String username, String password) {

        if (validateEmployee(empNo, firstName, lastName, username, password)) {
            Employee e = new Employee(Integer.parseInt(empNo), firstName,
                    lastName, username, password);
            employees.add(e);
            database.addEmployee(e);
            PrimeFaces.current()
                    .executeScript("PF('addEmployeeDialog').hide();");
        }
    }

    public void editEmployee(String empNo, String firstName, String lastName,
            String username, String password) {
        if (validateEmployee(empNo, firstName, lastName, username, password)) {
            Employee e = editEmployee;
            e.setEmpNumber(Integer.parseInt(empNo));
            e.setFirstName(firstName);
            e.setLastName(lastName);
            e.setUserName(username);
            e.setPassword(password);

            database.updateEmployee(e);

            PrimeFaces.current()
                    .executeScript("PF('editEmployeeDialog').hide();");
        }
    }

    public void deleteEmployee(Employee e) {
        employees.remove(e);
        database.removeEmployee(e);
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public Employee getEditEmployee() {
        return editEmployee;
    }

    public void setEditEmployee(Employee editEmployee) {
        this.editEmployee = editEmployee;

        PrimeFaces.current().executeScript("PF('editEmployeeDialog').show();");
    }

    private void addErrorMessage(String summary) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                summary, null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public boolean validateEmployee(String empNo, String firstName,
            String lastName, String username, String password) {
        if (isAnyNullOrWhitespace(empNo, firstName, lastName, username,
                password)) {
            addErrorMessage("All fields must be filled in");
            return false;
        } else if (!isInteger(empNo)) {
            addErrorMessage("Employee number must be an integer");
            return false;
        } else if (!isValidEmployee(new Employee(Integer.parseInt(empNo),
                firstName, lastName, username, password))) {
            addErrorMessage("Duplicate employee number or username found");
            return false;
        }

        return true;
    }

    private boolean isValidEmployee(Employee employee) {
        for (Employee e : employees) {
            if (e.getEmpNumber() == employee.getEmpNumber()) {
                return false;
            }

            if (e.getUserName().equalsIgnoreCase(employee.getUserName())) {
                return false;
            }
        }

        return true;
    }

    private boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private boolean isAnyNullOrWhitespace(String... values) {
        for (String s : values) {
            if (s == null || s.trim().length() == 0) {
                return true;
            }
        }

        return false;
    }
}