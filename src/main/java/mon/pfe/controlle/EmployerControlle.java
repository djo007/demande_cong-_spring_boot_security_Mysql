package mon.pfe.controlle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import mon.pfe.entity.Demande;
import mon.pfe.entity.Departement;
import mon.pfe.entity.Employer;
import mon.pfe.entity.Notification_Reprise;
import mon.pfe.entity.Solde;
import mon.pfe.entity.TypeCongee;
import mon.pfe.entity.Validation;
import mon.pfe.repository.DemandeRepository;
import mon.pfe.repository.DepartementRepository;
import mon.pfe.repository.EmployerRepository;
import mon.pfe.repository.NotificationRepriseRepository;
import mon.pfe.repository.SoldeRepository;
import mon.pfe.repository.TypeCongeeRepository;
import mon.pfe.repository.ValidationRepository;

@Controller
@Secured(value={"ROLE_PR","ROLE_RD","ROLE_EM"})
public class EmployerControlle {

	@Autowired
	public EmployerRepository employer;	
	
	@Autowired
	public DemandeRepository demande;
	
	@Autowired
	public LoginController log;
	
	@Autowired
	public NotificationRepriseRepository notification;
	
	@Autowired
	public TypeCongeeRepository typecongee;
	
	@Autowired
	public ValidationRepository validation;
	
	@Autowired
	public DepartementRepository departement;
	
	@Autowired
	public SoldeRepository solde;
	
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/remplir_demande_acteur" , method=RequestMethod.GET)
	public String remplir_demande_acteur(String date_envoit, String date_debut, int periode, Long id_employer,
			int id_type, Model model, HttpServletRequest httpServletRequest){

		
//		String dd="2016-05-13";
//		String df="2016-05-15";
//		Date dated=null;
//		Date datef=null;
//		
//		try {
//			dated=formatter.parse(dd);
//			datef=formatter.parse(df);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		long mil=1000*60*60*24;
//		long delta =datef.getTime()-dated.getTime();
//		System.out.println("deffirece : "+delta/mil);
//		
//		
//		System.out.println("date debeut = "+dated);
//		dated.setDate(dated.getDate()+30);
//		String s=formatter.format(dated);
//		try {
//			dated=formatter.parse(s);
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//			System.out.println("date apres ajout jours : "+dated);
	
		
		int d=demande.count_demande_en_cours(id_employer, 'p');
		
		String username=log.getLogedUser(httpServletRequest);
		model.addAttribute("username",username);
		List<Employer> ep=employer.find_by_nom_utilisateur(username);
		Employer emp=ep.get(0);
		List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
		model.addAttribute("notif",noti);
		List<Validation>valid=validation.find_by_id_pr(emp.getId_employer(),'p');
		model.addAttribute("valid",valid);
		

		
		if(d<1){
			TypeCongee type=typecongee.findOne(id_type);
			
			if(periode>type.getNmbre_jours()){
			
				SimpleDateFormat format = new SimpleDateFormat("yyyy");
				int annee= Integer.parseInt(format.format(new Date()));
				
				Solde s1= solde.find_by_id_employer_anne(emp.getId_employer(), annee);
				Solde s2= solde.find_by_id_employer_anne(emp.getId_employer(), (annee-1));
				 long i=type.getNmbre_jours();
				
			 switch (type.getCode_type()) {
			
			 case "011110": i = s1.getTotal_solde()+s2.getTotal_solde();
				
				break;
				
			case "000110": i = s1.getSolde_specifique();
			
			break;

			case "111100": i = s1.getSolde_materniter();
			
			break;

			case "011100": i=s1.getSolde_maladie();
				break;
			}
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date de = null;
			Date dd = null;
			try {
				 de = formatter.parse(date_envoit);
				 dd=formatter.parse(date_debut);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(i<periode){
				
				String chaine ="solde insuffisante vous ne pouvez pas passer cette demande ";
				model.addAttribute("chaine",chaine);
			}
			
			else {
				
			
				Date df=dd;
				 df.setDate(df.getDate()+periode);	
					demande.save(new Demande(de,dd,df,periode,id_employer,id_type,'p'));
					
					Demande dem = demande.demande_en_cours(id_employer, 'p');
					
//					Date date_validation_rd, Long id_demande, Long id_employer, Long id_resposable, char action_rd
					
					try {
						validation.save(new Validation(formatter.parse(formatter.format(new Date())),dem.getId_demande(),id_employer,emp.getIdresponsable(),'p'));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String chaine ="votre demande est en cours d'execution ...";
					model.addAttribute("chaine",chaine);
					
				}

			
			
			}//fin if de verification period demande et period autoriser pour congee
			
			else{
				String chaine ="periode demander depasser le nombre de jours autoriser pour ce type de congee";
				model.addAttribute("chaine",chaine);
				
			}
			
		}//fin if de verification de qu'il a une demande encours
		
		else {
			String chaine ="vous avez une demande en cours d'execution vous pouvez pas affecter autre";
			model.addAttribute("chaine",chaine);
		}
		
		return "etat_demande";
	}
	

	@RequestMapping(value="/demande_N1" , method=RequestMethod.GET)
	public String demande_n1(Long ntf, Model model, HttpServletRequest httpServletRequest){
		
		String username=log.getLogedUser(httpServletRequest);
		model.addAttribute("username",username);
		
		
		List<Employer> ep=employer.find_by_nom_utilisateur(username);
		Employer emp=ep.get(0);
		List<Employer> list = employer.findAll();
		model.addAttribute("emp",list);
		List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
		model.addAttribute("notif",noti);
		List<Validation>valid=validation.find_by_id_pr(emp.getId_employer(),'p');
		model.addAttribute("valid",valid);
		
		Validation notif=validation.findOne(ntf);
		Employer emp_env=employer.findOne(notif.getId_employer());
		Employer rd=employer.findOne(notif.getId_responsable());
		Demande dem=demande.findOne(notif.getId_demande());
		TypeCongee type=typecongee.findOne(dem.getId_type());
		Departement dp=departement.findOne(emp_env.getId_departement());
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date dt=new Date();
		model.addAttribute("date",formatter.format(dt));
		model.addAttribute("dp",dp);
		model.addAttribute("type",type);
		model.addAttribute("dem",dem);
		model.addAttribute("emp_env",emp_env);
		model.addAttribute("rd",rd);
		model.addAttribute("v",notif);
		return "demande_N1";
	}
	
	@RequestMapping(value="/validation_N1" , method=RequestMethod.GET)
	public String validation_N1(@RequestParam(required=true, value="action")String bt,Long id_validation,Long id_employer, Long id_demande,Long id_responsable, String avis_rd,
				Long id_remplacant,String date_validation_rd, Model model, HttpServletRequest httpServletRequest){		
		
		Employer emp =employer.findOne(id_employer);
		Departement dep =departement.findOne(emp.getId_departement());
	
		Validation vali=validation.findOne(id_validation);
		 if(vali.getAction_rd()=='p'){
		
		 if (bt.equals("accepter")){
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date dvrd = null;
				try {
					 dvrd = formatter.parse(date_validation_rd);
				
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			 validation.save(new Validation(id_validation,dvrd,id_demande,id_employer,id_remplacant,id_responsable,dep.getId_chef(),'o','p',avis_rd));
			
			 String chaine =" votre acceptation a ete bien saisi";
			 model.addAttribute("chaine",chaine);
			 
		 }
		 
		else{
			
			
			 
			 
			 Demande dem = demande.findOne(id_demande);
			
				demande.save(new Demande(dem.getId_demande(),dem.getDate_envoit(),dem.getDate_debut(),dem.getDate_fin(),dem.getPeriode(),dem.getId_employer(),dem.getId_type(),'n'));
				String chaine =" votre refus a ete bien saisi";
				 model.addAttribute("chaine",chaine);
				 
			
		}
		 
		 }
		  
		  else{
			  
				String chaine =" vous avez deja donne votre avis pour cette demande ";
				 model.addAttribute("chaine",chaine);
			  
		  }
		 
		 
		 List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
			model.addAttribute("notif",noti);
			List<Validation>valid=validation.find_by_id_pr(emp.getId_employer(),'p');
			model.addAttribute("valid",valid);
			
		 return "validation_N1";
	}
	
	@RequestMapping(value="/demande_N2" , method=RequestMethod.GET)
	public String demande_n2(Long vld, Model model, HttpServletRequest httpServletRequest){
		
		String username=log.getLogedUser(httpServletRequest);
		model.addAttribute("username",username);
		
		List<Employer> list1 = employer.findAll();
		model.addAttribute("emp",list1);
		List<Employer> ep=employer.find_by_nom_utilisateur(username);
		Employer emp=ep.get(0);
		
		
		
		Validation valid=validation.findOne(vld);
		Employer emp_env=employer.findOne(valid.getId_employer());
		Employer rd=employer.findOne(valid.getId_responsable());
		Employer pr=employer.findOne(valid.getId_premier_responsable());
		Employer rp=employer.findOne(valid.getId_remplaçant());
		Demande dem=demande.findOne(valid.getId_demande());
		TypeCongee type=typecongee.findOne(dem.getId_type());
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date dt=new Date();
		model.addAttribute("date",formatter.format(dt));
		model.addAttribute("validation",valid);
		model.addAttribute("type",type);
		model.addAttribute("dem",dem);
		model.addAttribute("emp_env",emp_env);
		model.addAttribute("rd",rd);
		model.addAttribute("pr",pr);
		model.addAttribute("rp",rp);
		
		List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
		model.addAttribute("notif",noti);
		List<Validation>valide=validation.find_by_id_pr(emp.getId_employer(),'p');
		model.addAttribute("valid",valide);
		return "demande_N2";
	}
	
	@RequestMapping(value="/validation_N2" , method=RequestMethod.GET)
	public String validation_N2(@RequestParam(required=true, value="action")String bt,Long id_validation, String date_validation_pr, String avis_pr, Model model, HttpServletRequest httpServletRequest){
	
		String username=log.getLogedUser(httpServletRequest);
		model.addAttribute("username",username);
		
	List<Employer> empl=employer.find_by_nom_utilisateur(username);
	Employer emp=empl.get(0);
	
		Validation valid=validation.findOne(id_validation);
		 if(valid.getAction_pr()=='p'){
		
		Demande dem = demande.findOne(valid.getId_demande());
		if (bt.equals("accepter")){
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date dvrd = null;
				try {
					 dvrd = formatter.parse(date_validation_pr);
				
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
			 validation.save(new Validation(id_validation,valid.getDate_validation_rd(),dvrd,valid.getId_demande(),valid.getId_employer(),valid.getId_remplaçant(),
					 valid.getId_responsable(),valid.getId_premier_responsable(),'o','o','p',valid.getAvis_rd(),avis_pr));
			  
			 String chaine =" votre acceptation a ete bien saisi";
			 model.addAttribute("chaine",chaine);
			 
			
		 }
		 
		else{
			
			 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date dvrd = null;
				try {
					 dvrd = formatter.parse(date_validation_pr);
				
				} catch (ParseException e) {
					e.printStackTrace();
				}
			validation.save(new Validation(id_validation,valid.getDate_validation_rd(),dvrd,valid.getId_demande(),valid.getId_employer(),valid.getId_remplaçant(),
					 valid.getId_responsable(),valid.getId_premier_responsable(),'o','n','n',valid.getAvis_rd(),avis_pr));
			demande.save(new Demande(dem.getId_demande(),dem.getDate_envoit(),dem.getDate_debut(),dem.getDate_fin(),dem.getPeriode(),dem.getId_employer(),dem.getId_type(),'n'));
			
			String chaine =" votre refus a ete bien saisi";
			 model.addAttribute("chaine",chaine);
			 
			
		}
		
		 }
		  
		  else{
			  
				String chaine =" vous avez deja donne votre avis pour cette demande ";
				 model.addAttribute("chaine",chaine);
			  
		  }
		
		List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
		model.addAttribute("notif",noti);
		List<Validation>valide=validation.find_by_id_pr(emp.getId_employer(),'p');
		model.addAttribute("valid",valide);
		
		return "validation_N2";
		
	}
	
	@RequestMapping(value="/modifier_profil", method=RequestMethod.GET)
	public String modifier_ressource_humaine(Long id_employer, String nom_utilisateur, String mot_de_passe, String adresse, String telephone, String grade, String nom, String prenom,
			Long idresponsable,int id_departement,int id_role, Model model, HttpServletRequest httpServletRequest){

	employer.save(new Employer(id_employer, nom_utilisateur, mot_de_passe, adresse, telephone, grade, nom, prenom, idresponsable, id_departement, id_role));
		String username=log.getLogedUser(httpServletRequest);
		model.addAttribute("username",username);
		List<Employer> list = employer.findAll();
		model.addAttribute("emp",list);
		List<Employer> ep=employer.find_by_nom_utilisateur(username);
		Employer emp=ep.get(0);
		List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
		model.addAttribute("notif",noti);
		List<Validation>valid=validation.find_by_id_pr(emp.getId_employer(),'p');
		model.addAttribute("valid",valid);
		
		return "modifier_profil";
	}
	
	@RequestMapping(value="/modifier_mot_de_passe")
	public String profil_ressource(Model model, HttpServletRequest httpServletRequest,String nouveaux){
		String username=log.getLogedUser(httpServletRequest);
		model.addAttribute("username",username);
		List<Employer> ep=employer.find_by_nom_utilisateur(username);
		Employer emp=ep.get(0);
		employer.save(new Employer(emp.getId_employer(), emp.getNom_utilisateur(), nouveaux, emp.getAdresse(), emp.getTelephone(), emp.getGrade(),
					emp.getNom(), emp.getPrenom(), emp.getIdresponsable(), emp.getId_departement(), emp.getId_role()));
		List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
		model.addAttribute("notif",noti);
		List<Validation>valid=validation.find_by_id_pr(emp.getId_employer(),'p');
		model.addAttribute("valid",valid);
		
	 return "modifier_profil";
 }
	
	
	@RequestMapping(value="/reprise")
	public String reprise(Model model, HttpServletRequest httpServletRequest,Long id_demande, String date_envoit,String date_retour){
		String username=log.getLogedUser(httpServletRequest);
		model.addAttribute("username",username);
		List<Employer> ep=employer.find_by_nom_utilisateur(username);
		Employer emp=ep.get(0);
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		
		Date date_rt = null;
		Date date_ev = null;	
		try {
			date_rt=format.parse(date_retour);
			date_ev=format.parse(date_envoit);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
			notification.save(new Notification_Reprise(emp.getId_employer(), id_demande,date_ev, date_rt));
		
		Demande d=demande.findOne(id_demande);
		
		demande.save(new Demande(id_demande,d.getDate_envoit(), d.getDate_debut(), d.getDate_fin(), d.getPeriode(), d.getId_employer(), d.getId_type(),'r'));
		
		TypeCongee type= typecongee.findOne(d.getId_type());
		
		long mil=1000*60*60*24;
		long delta = d.getDate_fin().getTime()-date_rt.getTime();
		long dif =delta/mil;
		String s= String.valueOf(dif);
		int def=Integer.parseInt(s);
		
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		int annee= Integer.parseInt(year.format(new Date()));
		Solde s1= solde.find_by_id_employer_anne(emp.getId_employer(), annee);
		
		if(type.getCode_type()=="011100"){
			solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde(), s1.getSolde_materniter(), s1.getSolde_specifique(), def+s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
		}
		
		if(type.getCode_type()=="000110"){
			solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde(), s1.getSolde_materniter(), s1.getSolde_specifique()+def, s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
		}
		
		if(type.getCode_type()=="111100"){
			solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde(), s1.getSolde_materniter()+def, s1.getSolde_specifique(), s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
		}
		
		if(type.getCode_type()=="011110"){
			
			Solde s2= solde.find_by_id_employer_anne(emp.getId_employer(), (annee-1));
				if(s2.getTotal_solde()==0){
					solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde()+def, s1.getSolde_materniter(), s1.getSolde_specifique(), s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
				}
				else{
					solde.save(new Solde(s2.getId_Solde(), s2.getTotal_solde()+def, s2.getSolde_materniter(), s2.getSolde_specifique(), s2.getSolde_maladie() , s2.getAnnee_solde(), s2.getId_employer(), s2.getMise_a_jours()));
				}
		}
		
		List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
		model.addAttribute("notif",noti);
		List<Validation>valid=validation.find_by_id_pr(emp.getId_employer(),'p');
		model.addAttribute("valid",valid);
	
		String chaine ="votre reprise est enregistrer correctement";
		model.addAttribute("chaine",chaine);
	
	return "etat_demande";
 }
	
	@RequestMapping(value="/annuler_demande")
	public String annuler_demande_rh(HttpServletRequest httpServletRequest, Model model,Long id_demande){
		String username=log.getLogedUser(httpServletRequest);
	model.addAttribute("username",username);
	List<Employer> ep=employer.find_by_nom_utilisateur(username);
	Employer emp=ep.get(0);
	
	Demande d=demande.findOne(id_demande);
	demande.save(new Demande(d.getId_demande(),d.getDate_envoit(),d.getDate_debut(),d.getDate_fin(),d.getPeriode(),d.getId_employer(),d.getId_type(),'a'));
	
	TypeCongee type= typecongee.findOne(d.getId_type());
	
	SimpleDateFormat year = new SimpleDateFormat("yyyy");
	int annee= Integer.parseInt(year.format(new Date()));
	Solde s1= solde.find_by_id_employer_anne(emp.getId_employer(), annee);
	
	if(type.getCode_type()=="011100"){
		solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde(), s1.getSolde_materniter(), s1.getSolde_specifique(), d.getPeriode()+s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
	}
	
	if(type.getCode_type()=="000110"){
		solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde(), s1.getSolde_materniter(), s1.getSolde_specifique()+d.getPeriode(), s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
	}
	
	if(type.getCode_type()=="111100"){
		solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde(), s1.getSolde_materniter()+d.getPeriode(), s1.getSolde_specifique(), s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
	}
	
	if(type.getCode_type()=="011110"){
		
		Solde s2= solde.find_by_id_employer_anne(emp.getId_employer(), (annee-1));
			if(s2.getTotal_solde()==0){
				solde.save(new Solde(s1.getId_Solde(), s1.getTotal_solde()+d.getPeriode(), s1.getSolde_materniter(), s1.getSolde_specifique(), s1.getSolde_maladie() , s1.getAnnee_solde(), s1.getId_employer(), s1.getMise_a_jours()));
			}
			else{
				solde.save(new Solde(s2.getId_Solde(), s2.getTotal_solde()+d.getPeriode(), s2.getSolde_materniter(), s2.getSolde_specifique(), s2.getSolde_maladie() , s2.getAnnee_solde(), s2.getId_employer(), s2.getMise_a_jours()));
			}
	}
	
	List<Demande>dem_to_annuler=demande.find_to_annule(emp.getId_employer(),new Date(),'o');		
	model.addAttribute("dem_to_annuler",dem_to_annuler);
	
	List<Demande>dem_cant_annuler=demande.cant_annul(emp.getId_employer(),new Date(),'a');		
	model.addAttribute("dem_cant_annuler",dem_cant_annuler);
	
	List<Demande>dem=demande.find_id_employer(emp.getId_employer());
	model.addAttribute("dem",dem);
	List<Validation> noti=validation.find_by_id_rd(emp.getId_employer(),'p');
	model.addAttribute("notif",noti);
	List<Validation>valid=validation.find_by_id_pr(emp.getId_employer(),'p');
	model.addAttribute("valid",valid);

	 return "list_demande";
 }
	
	
}
