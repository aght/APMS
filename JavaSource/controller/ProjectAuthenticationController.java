package controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import model.Employee;
import model.ProEmp;
//import model.ProAssi;
import model.Project;
import model.WPEmp;
import model.WorkPackage;

@Named("paController")
@SessionScoped
public class ProjectAuthenticationController implements Serializable {

	@Inject
	private DatabaseController database;

	private Employee currentEmployee;

	public ProjectAuthenticationController() {

	}

	@PostConstruct
	public void init() {
		currentEmployee = getLoggedInEmployee();

	}

	public boolean isSupervisor() {
		List<Employee> empList = this.database.getEmployees();
		for (Employee emp : empList) {
			if (emp.getSuperEmpNo() == currentEmployee.getEmpNumber()) {
				return true;

			}
		}
		return false;
	}

	public boolean isProjectManager() {
		List<Project> allProject = this.database.getAllProjects();
		for (Project p : allProject) {
			if (currentEmployee.getEmpNumber() == p.getProMgrEmpNo()) {
				return true;
			}
		}
		return false;
	}

	public boolean isProjectAssistant() {
		List<Project> allProject = this.database.getAllProjects();
		for (Project p : allProject) {
			if (currentEmployee.getEmpNumber() == p.getProAssiEmpNo()) {
				return true;
			}
		}
		return false;
	}

	public boolean isAssignedProjectEmployee() {
		List<ProEmp> pme = this.database.getAllProEmp();
		for(ProEmp pe : pme){
			if(pe.getProEmp().getEmpNo() == currentEmployee.getEmpNumber()) {
				return true;
			}
		}
		return false;
	}
	public boolean isPMorPA() {
		return isProjectManager() || isProjectAssistant();
	}

	public boolean isPAnotPM() {
		return !isProjectManager() && isProjectAssistant();
	}

	public boolean isREEmp() {
		int empNo = currentEmployee.getEmpNumber();
		List<WPEmp> wpList = this.database.getAllWPEmp();

		for (WPEmp wp : wpList) {
			if (wp.getEmpNo() == empNo) {
				return true;
			}
		}

		return false;
	}

	private static Employee getLoggedInEmployee() {
		return (Employee) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get(LoginController.USER_KEY);
	}

	public boolean canSeeProject() {
		return isSupervisor() || isProjectManager() || isProjectAssistant() || isREEmp();
	}

}
