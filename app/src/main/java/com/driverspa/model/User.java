package com.driverspa.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "user")
public class User{
	
	public User(){
		setImages(new ArrayList<Image>());
        setCars(new ArrayList<CarItem>());
	}
	
	@DatabaseField(id = true) 
    private String id;

//	@ForeignCollectionField(eager = true)
//	private ForeignCollection<PhotoUserOrm> preferredImage;

	@DatabaseField
	@SerializedName("car_number")
    private String carNumber;

    @DatabaseField
    private String avatar;

	@DatabaseField	
	@SerializedName("last_login")
    private String lastLogin;
	
	@DatabaseField	
	@SerializedName("first_name")
    private String firstName;

	@DatabaseField	
	@SerializedName("username")	
    private String userName;

	@DatabaseField	
    private String token;

	@DatabaseField
	@SerializedName("company_name")
    private String companyName;

	@DatabaseField
    private String email;

	@DatabaseField
	@SerializedName("car_model")	
    private String carModel;

	@DatabaseField
	@SerializedName("is_company")		
    private boolean isCompany;

	@DatabaseField
	@SerializedName("last_name")			
    private String lastName;
    
	@SerializedName("images")
    private ArrayList<Image> images;

	@DatabaseField
	@SerializedName("resource_uri")			
    private String resourceUri;

	@DatabaseField
	@SerializedName("mobile")
    private String phone;

    @DatabaseField
    @SerializedName("default_carwash_id")
    private String defaultCarwashId;

    @DatabaseField
    private String city;

    // Present only on the activate response (new password flow). Not persisted.
    @SerializedName("password_required")
    private Boolean passwordRequired;

    // Present only on the whatsapp_status response. false while the WhatsApp
    // verification is pending; true (alongside a token) once matched. Not persisted.
    @SerializedName("verified")
    private Boolean verified;

    List<CarItem> cars;

    public boolean isVerified() {
        return verified != null && verified;
    }

    public boolean isPasswordRequired() {
        return passwordRequired != null && passwordRequired;
    }

    public void setPasswordRequired(Boolean passwordRequired) {
        this.passwordRequired = passwordRequired;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<CarItem> getCars() {
        return cars;
    }

    public void setCars(List<CarItem> cars) {
        this.cars = cars;
    }

    public String getCarNumber()
    {
        return carNumber;
    }

    public void setCarNumber(String carNumber)
    {
        this.carNumber = carNumber;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getLastLogin ()
    {
        return lastLogin;
    }

    public void setLastLogin (String lastLogin)
    {
        this.lastLogin = lastLogin;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getFirstName ()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getUsername()
    {
        return userName;
    }

    public void setUsername(String userName)
    {
        this.userName = userName;
    }

    public String getToken ()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName (String companyName)
    {
        this.companyName = companyName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getCarModel()
    {
        return carModel;
    }

    public void setCarModel(String carModel)
    {
        this.carModel = carModel;
    }

    public boolean isCompany()
    {
        return isCompany;
    }

    public void setCompany(boolean isCompany)
    {
        this.isCompany = isCompany;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public ArrayList<Image> getImages()
    {
        return images;
    }

    public void setImages(ArrayList<Image> images)
    {
        this.images = images;
    }

    public String getResourceUri ()
    {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri)
    {
        this.resourceUri = resourceUri;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDefaultCarwashId() {
        return defaultCarwashId;
    }

    public void setDefaultCarwashId(String defaultCarwashId) {
        this.defaultCarwashId = defaultCarwashId;
    }

    public boolean isOwner(){
        return TextUtils.isEmpty(this.defaultCarwashId);
    }

    // GSON
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static User deserialize(String serializedData) {
		Gson gson = new Gson();
		return gson.fromJson(serializedData, User.class);
	}


}