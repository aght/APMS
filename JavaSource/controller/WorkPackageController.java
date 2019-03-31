//package controller;
//
//import java.io.Serializable;
//import java.util.List;
//
//import javax.annotation.PostConstruct;
//import javax.enterprise.context.SessionScoped;
//import javax.inject.Inject;
//import javax.inject.Named;
//
//import org.primefaces.model.DefaultTreeNode;
//import org.primefaces.model.TreeNode;
//
//import model.WorkPackage;
//
//@Named("wpController")
//@SessionScoped
//public class WorkPackageController implements Serializable {
//	@Inject
//	private DatabaseController database;
//
//	private TreeNode root;
//
//	@PostConstruct
//	public void init() {
//		root = new DefaultTreeNode(new WorkPackage(), null);
//		for (WorkPackage wp : database.getRootWP()) {
//			TreeNode wpNode = new DefaultTreeNode(wp, root);
//			getTree(wpNode, wp);
//		}
//	}
//
//	public void getTree(TreeNode parentNode, WorkPackage parentWp) {
//		List<WorkPackage> childList = database.getLowerWP(parentWp.getWpid());
//		for (WorkPackage childWp : childList) {
//			TreeNode childNode = new DefaultTreeNode(childWp, parentNode);
//			getTree(childNode, childWp);
//		}
//	}
//
//	public TreeNode getRoot() {
//		return root;
//	}
//
//	public void deleteWp(WorkPackage wp) {
//		System.out.println(wp);
//		this.database.deleteWorkPackage(wp.getWorkPackagePk());
//	}
//}
