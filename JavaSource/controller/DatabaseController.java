package controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;

import com.sun.corba.se.spi.orbutil.threadpool.Work;

import model.Employee;
import model.PLevel;
import model.ProAssi;
import model.ProEmp;
import model.Project;
import model.Signature;
import model.Timesheet;
import model.TimesheetRow;
import model.TimesheetRowPK;
import model.WorkPackage;
import model.WorkPackagePK;
import utils.DateUtils;

@Stateless
public class DatabaseController implements Serializable {
	@PersistenceContext(unitName = "apms", type = PersistenceContextType.TRANSACTION)
	private EntityManager manager;

	// #########################################################################
	// # Employee methods
	// #########################################################################
	public List<Employee> getEmployees() {
		return manager.createQuery("SELECT e FROM Employee e", Employee.class).getResultList();
	}

	public List<Employee> getActiveEmployees() {
		return manager.createQuery("SELECT e FROM Employee e WHERE e.state = 'Active'", Employee.class).getResultList();
	}

	public Employee getEmployeeByUsername(String username) {
		TypedQuery<Employee> query = manager.createQuery("select e from Employee e where e.userName = :username",
				Employee.class);
		query.setParameter("username", username);
		try {
			Employee emp = query.getSingleResult();
			return emp;
		} catch (Exception exp) {
			return null;
		}

	}

	public Employee getEmployeeById(int id) {
		TypedQuery<Employee> query = manager.createQuery("select e from Employee e where e.empNumber = :empNumber",
				Employee.class);
		query.setParameter("empNumber", id);
		try {
			Employee emp = query.getSingleResult();
			return emp;
		} catch (Exception exp) {
			return null;
		}
	}

	public void addEmployee(Employee e) {
		manager.persist(e);
	}

	public void removeEmployee(Employee e) {
		manager.remove(manager.contains(e) ? e : manager.merge(e));
	}

	public void updateEmployee(Employee e) {
		manager.merge(e);
	}

	// #########################################################################
	// # Timesheet methods
	// #########################################################################
	public List<Timesheet> getTimesheets(int empNo) {
		List<Timesheet> timesheets = manager.createQuery("SELECT t from Timesheet t", Timesheet.class).getResultList();

		List<Timesheet> result = new ArrayList<Timesheet>();
		for (Timesheet timesheet : timesheets) {
			if (timesheet.getTimesheetPk().getEmpNo() == empNo) {
				result.add(timesheet);
			}
		}

		return result;
	}

	public void addTimesheet(Timesheet t) {
		manager.persist(t);
	}

	public void addTimesheetIfNotExist(Timesheet t, boolean merge) {
		if (manager.find(Timesheet.class, t.getTimesheetPk()) == null) {
			manager.persist(t);
		} else if (merge) {
			manager.merge(t);
		}
	}

	public void removeTimesheet(Timesheet t) {
		manager.remove(manager.contains(t) ? t : manager.merge(t));
	}

	/**
	 * merge Category record fields into existing database record.
	 * 
	 * @param category the record to be merged.
	 */
	public void updateTimesheet(Timesheet t) {
		manager.merge(t);
	}

	// #########################################################################
	// # TimesheetRow methods
	// #########################################################################
	private List<TimesheetRow> getTimesheetRows() {
		return manager.createQuery("SELECT tr from TimesheetRow tr", TimesheetRow.class).getResultList();
	}

	public List<TimesheetRow> getTimesheetRows(int empNo, Date startDate) {
		List<TimesheetRow> timesheetRows = getTimesheetRows();

		List<TimesheetRow> relatedTimesheetRows = new ArrayList<TimesheetRow>();

		Date start = DateUtils.getTimesheetStartDate(startDate);
		Date end = DateUtils.getTimesheetEndDate(startDate);

		for (TimesheetRow row : timesheetRows) {
			TimesheetRowPK pk = row.getTimesheetRowPk();

			if (pk.getEmpNo() == empNo && DateUtils.isWithinRange(pk.getStartDate(), start, end)) {
				relatedTimesheetRows.add(row);
			}
		}

		return relatedTimesheetRows;
	}

	public void addIfNotExistTimesheetRows(List<TimesheetRow> rows) {
		for (TimesheetRow row : rows) {
			if (manager.find(TimesheetRow.class, row.getTimesheetRowPk()) != null) {
				manager.merge(row);
			} else {
				manager.persist(row);
			}
		}
	}

	public void removeTimesheetRows(List<TimesheetRow> rows) {
		for (TimesheetRow row : rows) {
			manager.remove(manager.contains(row) ? row : manager.merge(row));
		}
	}

	public void removeTimesheetRows(Timesheet t) {
		removeTimesheetRows(getTimesheetRows(t.getTimesheetPk().getEmpNo(), t.getTimesheetPk().getStartDate()));
	}

	// #########################################################################
	// # Project methods
	// #########################################################################

	/**
	 * GET
	 * 
	 * @return all projects
	 */
	public List<Project> getAllProjects() {
		List<Project> projects = this.manager.createQuery("SELECT p from Project p", Project.class).getResultList();

		return projects;
	}

	/**
	 * GET
	 * 
	 * @param proNo
	 * @return project by ID
	 */
	public Project findByProjectNo(final int proNo) {

		return this.manager.find(Project.class, proNo);
	}

	/**
	 * POST add a new project
	 * 
	 * @param newProject
	 */
	public boolean persistProject(Project newProject) {
		Project p = this.manager.find(Project.class, newProject.getProNo());
		if (p == null) {
			this.manager.persist(newProject);
			return true;
		}
		return false;

	}

	public List<Project> getProjectsBySupervisor(int id) {
		List<ProEmp> proemp = new ArrayList<ProEmp>();
		TypedQuery<ProEmp> query = this.manager.createQuery("SELECT p from ProEmp p WHERE p.pk.empNo =:empNo",
				ProEmp.class);
		query.setParameter("empNo", id);
		try {
			proemp = query.getResultList();
			List<Project> projects = new ArrayList<Project>();
			for (ProEmp pe : proemp) {
				projects.add(findByProjectNo(pe.getPk().getProNo()));
			}

			return projects;
		} catch (Exception exp) {
			return null;
		}
	}

	/**
	 * POST
	 * 
	 * @param project
	 * @return UPDATE A PROJECT
	 */
	public boolean updateProject(Project project) {
		Project p = this.manager.find(Project.class, project.getProNo());
		if (p != null) {
			this.manager.merge(project);
			return true;
		}
		return false;
	}

	public boolean deleteProjectByProNo(final int proNo) {
		Project p = this.findByProjectNo(proNo);
		try {
			this.manager.remove(p);
			this.manager.flush();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<ProAssi> getAllProAssi() {
		List<ProAssi> projects = this.manager.createQuery("SELECT p from ProAssi p", ProAssi.class).getResultList();
		return projects;
	}

	public List<ProAssi> getProAssiByProNo(int proNo) {
		List<ProAssi> pa = new ArrayList<ProAssi>();
		for (ProAssi p : getAllProAssi()) {
			if (p.getPk().getProNo() == proNo) {
				pa.add(p);
			}
		}
		return pa;

	}

	public List<Employee> getEmployeeByProAssi(int proNo) {
		List<Employee> el = new ArrayList<Employee>();
		System.out.println("Hel" + getProAssiByProNo(proNo));
		for (ProAssi proass : getProAssiByProNo(proNo)) {
			System.out.println("Emp" + getEmployeeById(proass.getPk().getEmpNo()));
			el.add(getEmployeeById(proass.getPk().getEmpNo()));
		}
		return el;
	}

	public boolean persistProAss(ProAssi proass) {
		ProAssi p = this.manager.find(ProAssi.class, proass);
		if (p == null) {
			this.manager.persist(proass);
			return true;
		}
		return false;
	}

	public boolean deleteProAss(ProAssi proAssi) {
		if (getProAssiByProNo(proAssi.getPk().getProNo()) != null) {
			try {
				this.manager.remove(proAssi);
				this.manager.flush();
				return true;

			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	// #########################################################################
	// # WorkPackage methods
	// #########################################################################

	/**
	 * POST add a new project
	 * 
	 * @param newProject
	 */
	public List<WorkPackage> getAllWp() {
		return this.manager.createQuery("SELECT wp from WorkPackage wp", WorkPackage.class).getResultList();
	}

	public List<WorkPackage> getWPByProject(int proNo) {
		List<WorkPackage> wp = new ArrayList<WorkPackage>();
		TypedQuery<WorkPackage> query = this.manager
				.createQuery("SELECT wp from WorkPackage wp WHERE wp.workPackagePk.proNo =:proNo", WorkPackage.class);
		System.out.println(proNo);
		query.setParameter("proNo", proNo);
		try {
			wp = query.getResultList();
			return wp;
		} catch (Exception exp) {
			return null;
		}
	}

	public List<WorkPackage> getLowerWP(String wpid) {
		List<WorkPackage> result = new ArrayList<WorkPackage>();
		if (wpid != null) {
			for (WorkPackage wp : getAllWp()) {
				String parentId = wp.getParentWPID();
				if (parentId != null && parentId.equals(wpid)) {
					result.add(wp);
				}
			}
		}
		return result;
	}

	public List<WorkPackage> getRootWP() {
		List<WorkPackage> result = new ArrayList<WorkPackage>();
		for (WorkPackage wp : getAllWp()) {
			if (wp.getParentWPID() == null) {
				result.add(wp);
			}
		}
		return result;
	}

	public List<WorkPackage> getRootWPByProNo(int proNo) {
		List<WorkPackage> result = new ArrayList<WorkPackage>();
		for (WorkPackage wp : getAllWp()) {
			if (wp.getParentWPID() == null && wp.getProNo() == proNo) {
				result.add(wp);
			}
		}
		return result;
	}

	public boolean persistWP(WorkPackage wp) {
		WorkPackage checkWp = this.manager.find(WorkPackage.class, wp.getWorkPackagePk());
		if (checkWp == null) {
			this.manager.persist(wp);
			return true;
		}
		return false;
	}

	public boolean updateWP(WorkPackage wp) {
		WorkPackage checkWp = this.manager.find(WorkPackage.class, wp.getWorkPackagePk());
		if (checkWp != null) {
			this.manager.merge(wp);
			return true;
		}
		return false;
	}

	public boolean deleteWorkPackage(final WorkPackagePK key) {
		WorkPackage wp = this.manager.find(WorkPackage.class, key);
		try {
			this.manager.remove(wp);
			this.manager.flush();
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public List<WorkPackage> getWPByEmpNo(int empNo) {
		List<WorkPackage> wpList = new ArrayList<WorkPackage>();
		for (WorkPackage wp : getAllWp()) {
			if (wp.getReEmpNo() == empNo) {
				wpList.add(wp);
			}
		}
		return wpList;
	}

	
	// #########################################################################
	// # PLevel methods
	// #########################################################################
	public List<PLevel> getPLevels() {
		return manager.createQuery("SELECT p FROM PLevel p", PLevel.class).getResultList();
	}

	public PLevel getPLevelByPK(Date startDate, String pLevel) {
		TypedQuery<PLevel> query = manager.createQuery(
				"select p from PLevel p where p.StartDate = :StartDate AND p.PLevel = :PLevel", PLevel.class);
		query.setParameter("StartDate", startDate);
		query.setParameter("PLevel", pLevel);
		return query.getSingleResult();
	}

	public void updatePLevel(PLevel e) {
		manager.merge(e);
	}

	public void addPLevel(PLevel e) {
		manager.persist(e);
	}

	public void removePLevel(PLevel e) {
		manager.remove(manager.contains(e) ? e : manager.merge(e));
	}

	public void addSignature(final Signature newSignature) {
		manager.persist(newSignature);
	}

}

/*
 * This is just a place holder for extra whitespace
 * 
 * f
 * 
 * g
 * 
 * g
 * 
 * g
 * 
 * g
 * 
 * g
 * 
 * g
 * 
 * g
 * 
 * g
 * 
 * g
 */