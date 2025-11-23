package com.driverspa.model;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.driverspa.BA;
import com.driverspa.util.L;
import com.driverspa.util.otto.DummyWashersRequestEvent;

/**
 * Created by Yerzhan Tanatov on 30/06/16.
 */
public class TmpMyWash {

    public static void createInfoWashers(String city){
        String data = getFileIput("washme/"+city+"/carwashes.json");
        L.d("data is "+data);
//        data = data.substring(1,data.length()-1);
        data = "{\"washers\":"+data+"}";
        L.d("new data is "+data);
        TmpGoWash goWash = TmpGoWash.deserialize(data.toString());
        List<Washer> request = new ArrayList<Washer>();
        for(TmpGoWash.GWash washer:goWash.getWashers()) {
            Washer myWash = new Washer();
            myWash.setName(washer.getName());

            Washer.Contact contact = new Washer.Contact();
            contact.setType("phone");
            contact.setValue(washer.getPhones().get(0));
            ArrayList<Washer.Contact> contacts = new ArrayList< Washer.Contact>();
            contacts.add(contact);
            myWash.setContacts(contacts);

            ArrayList<Box> boxSettings = new ArrayList<Box>();
            for (int i = 0; i < 1; i++) {
                Box boxSetting = new Box();
                boxSetting.setBookingType(BookingType.Offline);
                boxSettings.add(boxSetting);
            }
            myWash.setBoxSettings(boxSettings);

            ArrayList<Double> lonlat = new ArrayList<Double>();
            lonlat.add(washer.getLongitude());
            lonlat.add(washer.getLatitude());
            myWash.setLonLat(lonlat);
            myWash.setCity(city);
            myWash.setWasherPublic(true);
            myWash.setRating(0.0);
            myWash.setAddress(washer.getAddress());

            //Time
            ArrayList<Washer.Day> mon;
            ArrayList<Washer.Day> tue;
            ArrayList<Washer.Day> wed;
            ArrayList<Washer.Day> thu;
            ArrayList<Washer.Day> fri;
            ArrayList<Washer.Day> sat;
            ArrayList<Washer.Day> sun;

            mon = new ArrayList<Washer.Day>();
            mon.add(new Washer.Day());
            tue = new ArrayList<Washer.Day>();
            tue.add(new Washer.Day());
            wed = new ArrayList<Washer.Day>();
            wed.add(new Washer.Day());
            thu = new ArrayList<Washer.Day>();
            thu.add(new Washer.Day());
            fri = new ArrayList<Washer.Day>();
            fri.add(new Washer.Day());
            sat = new ArrayList<Washer.Day>();
            sat.add(new Washer.Day());
            sun = new ArrayList<Washer.Day>();
            sun.add(new Washer.Day());
            Washer.Schedule schedule = new Washer.Schedule();

            myWash.setSchedule(schedule);
            for(TmpGoWash.WorkingHour h: washer.getWorkingHours()){
                if(h.getDay() == 1){
                    mon.get(0).setFrom(h.getStart());
                    mon.get(0).setTo(h.getEnd()==1440?h.getEnd()-1:h.getEnd());
                }
                else if(h.getDay() == 2){
                    tue.get(0).setFrom(h.getStart());
                    tue.get(0).setTo(h.getEnd()==1440?h.getEnd()-1:h.getEnd());
                }
                else if(h.getDay() == 3){
                    wed.get(0).setFrom(h.getStart());
                    wed.get(0).setTo(h.getEnd()==1440?h.getEnd()-1:h.getEnd());
                }
                else if(h.getDay() == 4){
                    thu.get(0).setFrom(h.getStart());
                    thu.get(0).setTo(h.getEnd()==1440?h.getEnd()-1:h.getEnd());
                }
                else if(h.getDay() == 5){
                    fri.get(0).setFrom(h.getStart());
                    fri.get(0).setTo(h.getEnd()==1440?h.getEnd()-1:h.getEnd());
                }
                else if(h.getDay() == 6){
                    sat.get(0).setFrom(h.getStart());
                    sat.get(0).setTo(h.getEnd()==1440?h.getEnd()-1:h.getEnd());
                }
                else if(h.getDay() == 7){
                    sun.get(0).setFrom(h.getStart());
                    sun.get(0).setTo(h.getEnd()==1440?h.getEnd()-1:h.getEnd());
                }
            }

            myWash.getSchedule().setMonday(mon);
            myWash.getSchedule().setTuesday(tue);
            myWash.getSchedule().setWednesday(wed);
            myWash.getSchedule().setThursday(thu);
            myWash.getSchedule().setFriday(fri);
            myWash.getSchedule().setSaturday(sat);
            myWash.getSchedule().setSunday(sun);
            myWash.getSchedule().setComment(washer.getInfo());

            //Sevices //Cartypes //Price
            ArrayList<Integer> myServices = new ArrayList<Integer>();
            ArrayList<Integer> myCars = new ArrayList<Integer>();
            HashMap<Integer,String> sMap = new HashMap<Integer,String>();
            HashMap<Integer,String> cMap = new HashMap<Integer,String>();
            HashMap<Integer, ArrayList<Washer.Prices>> carTypeMenu = new HashMap<Integer, ArrayList<Washer.Prices>>();
            ArrayList<Washer.Prices> prices = new ArrayList<Washer.Prices>();
            Integer tmpCarType = 0;
            Integer carType = 0;
            Integer serviceType = 0;

            int i = 0;
            for(TmpGoWash.Serv service : washer.getServices()){

                //Service
                if(service.getName().trim().toLowerCase().equals("Мойка кузова ".trim().toLowerCase())){
                    serviceType = 1;
                   if(sMap.get(1) == null) {
                       myServices.add(1);
                       sMap.put(1,service.getName());
                    }
                 }
                else if(service.getName().trim().toLowerCase().equals("Мойка салона ".trim().toLowerCase())){
                    serviceType = 2;
                   if(sMap.get(2) == null) {
                       myServices.add(2);
                        sMap.put(2,service.getName());
                    }
                 }
                else if(service.getName().trim().toLowerCase().equals("Мойка кузова + салона".trim().toLowerCase())){
                    serviceType = 3;
                   if(sMap.get(3) == null) {
                       myServices.add(3);
                        sMap.put(3,service.getName());
                    }
                 }
                else if(service.getName().trim().toLowerCase().equals("Мойка багажника".trim().toLowerCase())){
                    serviceType = 4;
                   if(sMap.get(4) == null) {
                       myServices.add(4);
                        sMap.put(4,service.getName());
                    }
                 }
                else if(service.getName().trim().toLowerCase().equals("Мойка двигателя".trim().toLowerCase())){
                    serviceType = 5;
                   if(sMap.get(5) == null) {
                       myServices.add(5);
                        sMap.put(5,service.getName());
                    }
                 }
                else serviceType = 0;

                //Cars
                if(service.getDescription().equals("Легковой автомобиль")){
                    carType = 1;
                    if(cMap.get(1) == null) {
                        myCars.add(1);
                        cMap.put(1,service.getDescription());
                    }
                }
                else if(service.getDescription().equals("Внедорожник")){
                    carType = 2;
                    if(cMap.get(2) == null) {
                        myCars.add(2);
                        cMap.put(2,service.getDescription());
                    }
                }
                else if(service.getDescription().equals("Кроссовер")){
                    carType = 3;
                    if(cMap.get(3) == null) {
                        myCars.add(3);
                        cMap.put(3,service.getDescription());
                    }
                }
                else if(service.getDescription().equals("Минивэн")){
                    carType = 4;
                    if(cMap.get(4) == null) {
                        myCars.add(4);
                        cMap.put(4,service.getDescription());
                    }
                }
                else{
                    carType = 0;
                }

                //Menu
                if(tmpCarType == 0){
                    prices = new ArrayList<Washer.Prices>();
                    L.d("*****************************************");
                }
                else
                if((tmpCarType != carType)){
                    carTypeMenu.put(tmpCarType, prices);
                    prices = new ArrayList<Washer.Prices>();
                    L.d("=========================================");
                }

               if(serviceType != 0 || carType != 0) {
                   Washer.Prices price = new Washer.Prices();
                   price.setTime(service.getDuration());
                   L.d("car type vs service " + carType + " " + serviceType);
                   price.setType(serviceType);
                   price.setPrice(Double.parseDouble(service.getPrice()));
                   prices.add(price);
               }
                tmpCarType = carType;

                //Last position
                if(i + 1 == washer.getServices().size())
                    carTypeMenu.put(carType, prices);
                i++;
            }

//            myWash.setMenu(carTypeMenu);
            myWash.setCarTypes(myCars);
            myWash.setServices(myServices);

            //Additional info
            final ArrayList<String> requestAddInfoTypes = new ArrayList<String>();
            requestAddInfoTypes.add("cash");
            requestAddInfoTypes.add("nocash");
            myWash.setPayOptions(requestAddInfoTypes);

            request.add(myWash);
        }
        BA.getEventBus().post(new DummyWashersRequestEvent(request));
    }


    private static void writeOutput(String str, String directory){
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + directory);
        try{
            FileOutputStream fos = new FileOutputStream(f);
            byte[] contentInBytes = str.getBytes();
            fos.write(contentInBytes);
            fos.flush();
            fos.close();
        }
        catch (Exception e){

        }
    }

    private static String getFileIput(String file){
        String data = "";
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + file );
        try {
            FileInputStream fis = new FileInputStream(f);

            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            data = fileContent.toString();

            fis.close();

        }
        catch(Exception e){
            L.d("can't load data ");
            L.d(e.getMessage());
        }
        return data;
    }


//    List<Washer> astanaWashers;
//    public List<Washer> getAstanaWashers() {
//        return astanaWashers;
//    }
//
//    public void setAstanaWashers(List<Washer> astanaWashers) {
//        this.astanaWashers = astanaWashers;
//    }
//
//    public static void loadData(){
//
////        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "washme/astana/carwashes.json");
////        FileInputStream fis = null;
////
////        try {
////            fis = new FileInputStream(file);
////
////            L.d("Total file size to read (in bytes) : "
////                    + fis.available());
////
////            int content;
////            StringBuffer fileContent = new StringBuffer("");
////            while ((content = fis.read()) != -1) {
////                // convert to char and display it
////                System.out.print((char) content);
////            }
////            L.d("file input "+fileContent.toString());
////        } catch (IOException e) {
////            L.d("can't load data ");
////            e.printStackTrace();
////        } finally {
////            try {
////                if (fis != null)
////                    fis.close();
////            } catch (IOException ex) {
////                ex.printStackTrace();
////            }
////        }
//
//        String data = getFileIput("washme/astana/carwashes.json");
//        TmpGoWash goWash = TmpGoWash.deserialize(data.toString());
//
//        List<TmpWasher> request = new ArrayList<TmpWasher>();
//        for(TmpGoWash.GWash washer:goWash.getWashers()){
////            L.d("name "+washer.getName());
////            String userId = UUID.randomUUID().toString();
////            String washerId = UUID.randomUUID().toString();
////
////            User user = new User();
////            user.set_id("ObjectId(\""+userId+"\")");
////            user.set_cls("User.user");
////            user.setUsername(washer.getPhones().get(0));
////            user.setIs_staff(false);
////            user.setIs_active(true);
////            user.setIs_superuser(false);
////            user.setMobile(washer.getPhones().get(0));
////            user.setIs_company(true);
////
////            //images
////            User.Image image = new User.Image();
////            image.setCarwash(washerId);
////            image.setId(UUID.randomUUID().toString());
////            User.ImageDetail detail = new User.ImageDetail();
////            detail.setPath("upload/original/"+washer.getImages().get(0));
////            List<Integer> size = new ArrayList<Integer>();
////            size.add(613);
////            size.add(408);
////            image.setOriginal(detail);
////            image.setThumb1(detail);
////            image.setThumb2(detail);
////
////            user.setActivation_att(0);
////            user.setActivation_code("000000");
//
//            TmpWasher myWash = new TmpWasher();
////            myWash.set_id("ObjectId(\""+washerId+"\")");
////            myWash.setUser("ObjectId(\""+userId+"\")");
//            myWash.setName(washer.getName());
//
//            TmpWasher.Schedule.Contact contact = new TmpWasher.Schedule.Contact();
//            contact.setType("phone");
//            contact.setValue(washer.getPhones().get(0));
//            List<TmpWasher.Schedule.Contact> contacts = new ArrayList<TmpWasher.Schedule.Contact>();
//            contacts.add(contact);
//            myWash.setContacts(contacts);
//            ArrayList<com.driverspa.model.Washer.BoxSettings> boxSettings = new ArrayList<com.driverspa.model.Washer.BoxSettings>();
//            for (int i = 0; i < 1; i++) {
//                com.driverspa.model.Washer.BoxSettings boxSetting = new com.driverspa.model.Washer.BoxSettings();
//                boxSetting.setBookingType(BookingType.Hybrid);
//                boxSettings.add(boxSetting);
//            }
//            myWash.setBoxSettings(boxSettings);
//
//            List<Double> lonlat = new ArrayList<Double>();
//            lonlat.add(washer.getLongitude());
//            lonlat.add(washer.getLatitude());
//
//            myWash.setLonlat(lonlat);
//            myWash.setCity("astana");
//
//            myWash.setIs_public(true);
//            myWash.setRating(5);
//            myWash.setIs_registered(false);
//            myWash.setStatus("pending");
//            myWash.setAddress(washer.getAddress());
//            request.add(myWash);
//        }
//
////        BA.getEventBus().post(new DummyWashersRequestEvent(request));
////        myobj.setAstanaWashers(astanaWashers);
////        writeOutput(myobj.serialize(),"washme/astana/output.json");
////        L.d(myobj.serialize());
//    }
//
//
//
//
//
//    private static class User{
//        String _id;
//        String _cls;
//        String username;
//        String password;
//        boolean is_staff;
//        boolean is_active;
//        boolean is_superuser;
//        List<String> user_permissions;
//        String api_key;
//        String mobile;
//        List<String> cars;
//        boolean is_company;
//        String company_name;
//        List<String> bookmarks;
//        Integer activation_att;
//        String activation_code;
//        String settings;
//        List<String> devices;
//
//
//
//        public String getSettings() {
//            return settings;
//        }
//
//        public void setSettings(String settings) {
//            this.settings = settings;
//        }
//
//        public List<String> getDevices() {
//            return devices;
//        }
//
//        public void setDevices(List<String> devices) {
//            this.devices = devices;
//        }
//
//
//
//        public String get_id() {
//            return _id;
//        }
//
//        public void set_id(String _id) {
//            this._id = _id;
//        }
//
//        public String get_cls() {
//            return _cls;
//        }
//
//        public void set_cls(String _cls) {
//            this._cls = _cls;
//        }
//
//        public String getUsername() {
//            return username;
//        }
//
//        public void setUsername(String username) {
//            this.username = username;
//        }
//
//        public String getPassword() {
//            return password;
//        }
//
//        public void setPassword(String password) {
//            this.password = password;
//        }
//
//        public boolean is_staff() {
//            return is_staff;
//        }
//
//        public void setIs_staff(boolean is_staff) {
//            this.is_staff = is_staff;
//        }
//
//        public boolean is_active() {
//            return is_active;
//        }
//
//        public void setIs_active(boolean is_active) {
//            this.is_active = is_active;
//        }
//
//        public boolean is_superuser() {
//            return is_superuser;
//        }
//
//        public void setIs_superuser(boolean is_superuser) {
//            this.is_superuser = is_superuser;
//        }
//
//        public List<String> getUser_permissions() {
//            return user_permissions;
//        }
//
//        public void setUser_permissions(List<String> user_permissions) {
//            this.user_permissions = user_permissions;
//        }
//
//        public String getApi_key() {
//            return api_key;
//        }
//
//        public void setApi_key(String api_key) {
//            this.api_key = api_key;
//        }
//
//        public String getMobile() {
//            return mobile;
//        }
//
//        public void setMobile(String mobile) {
//            this.mobile = mobile;
//        }
//
//        public List<String> getCars() {
//            return cars;
//        }
//
//        public void setCars(List<String> cars) {
//            this.cars = cars;
//        }
//
//        public boolean is_company() {
//            return is_company;
//        }
//
//        public void setIs_company(boolean is_company) {
//            this.is_company = is_company;
//        }
//
//        public String getCompany_name() {
//            return company_name;
//        }
//
//        public void setCompany_name(String company_name) {
//            this.company_name = company_name;
//        }
//
//        public List<String> getBookmarks() {
//            return bookmarks;
//        }
//
//        public void setBookmarks(List<String> bookmarks) {
//            this.bookmarks = bookmarks;
//        }
//
//        public Integer getActivation_att() {
//            return activation_att;
//        }
//
//        public void setActivation_att(Integer activation_att) {
//            this.activation_att = activation_att;
//        }
//
//        public String getActivation_code() {
//            return activation_code;
//        }
//
//        public void setActivation_code(String activation_code) {
//            this.activation_code = activation_code;
//        }
//
//        public String serialize() {
//            Gson gson = new Gson();
//            return gson.toJson(this);
//        }
//
//        public static User deserialize(String serializedData) {
//            Gson gson = new Gson();
//            return gson.fromJson(serializedData, User.class);
//        }
//
//        private static class Image {
//            private String id;
//            private String carwash;
//
//            private ImageDetail original;
//            private ImageDetail thumb1;
//            private ImageDetail thumb2;
//
//            public String getCarwash() {
//                return carwash;
//            }
//
//            public void setCarwash(String carwash) {
//                this.carwash = carwash;
//            }
//
//            public ImageDetail getOriginal() {
//                return original;
//            }
//
//            public void setOriginal(ImageDetail original) {
//                this.original = original;
//            }
//
//            public ImageDetail getThumb1() {
//                return thumb1;
//            }
//
//            public void setThumb1(ImageDetail thumb1) {
//                this.thumb1 = thumb1;
//            }
//
//            public ImageDetail getThumb2() {
//                return thumb2;
//            }
//
//            public void setThumb2(ImageDetail thumb2) {
//                this.thumb2 = thumb2;
//            }
//
//            public String getId() {
//                return id;
//            }
//
//            public void setId(String id) {
//                this.id = id;
//            }
//        }
//
//        private static class ImageDetail {
//            private ArrayList<String> size;
//            private String path;
//
//            public ImageDetail(){
//
//            }
//
//            public String getPath() {
//                return path;
//            }
//
//            public void setPath(String path) {
//                this.path = path;
//            }
//
//            public ArrayList<String> getSize() {
//                return size;
//            }
//
//            public void setSize(ArrayList<String> size) {
//                this.size = size;
//            }
//
//        }
//    }
//
//
//    public static class TmpWasher {
//
////        String _id;
////        String user;
//        String name;
//        String address;
//        List<Double> lonlat;
//        List<Integer> services;
//        List<Integer> cartypes;
//        HashMap<Integer, ArrayList<Prices>> menu;
//        boolean is_public;
//        public ArrayList<Review> reviews;
//        Integer rating;
//        boolean is_registered;
//        String status;
//        List<TmpWasher.Schedule.Contact> contacts;
//        String city;
//
//        List<String> payoptions;
//        @SerializedName("box_settings")
//        private ArrayList<Washer.BoxSettings> boxSettings;
//        List<String> stocks;
//
//        public ArrayList<Washer.BoxSettings> getBoxSettings() {
//            return boxSettings;
//        }
//
//        public void setBoxSettings(ArrayList<Washer.BoxSettings> boxSettings) {
//            this.boxSettings = boxSettings;
//        }
//
//        public List<TmpWasher.Schedule.Contact> getContacts() {
//            return contacts;
//        }
//
//        public void setContacts(List<TmpWasher.Schedule.Contact> contacts) {
//            this.contacts = contacts;
//        }
////
////        public String get_id() {
////            return _id;
////        }
////
////        public void set_id(String _id) {
////            this._id = _id;
////        }
////
////        public String getUser() {
////            return user;
////        }
////
////        public void setUser(String user) {
////            this.user = user;
////        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getAddress() {
//            return address;
//        }
//
//        public void setAddress(String address) {
//            this.address = address;
//        }
//
//        public List<Double> getLonlat() {
//            return lonlat;
//        }
//
//        public void setLonlat(List<Double> lonlat) {
//            this.lonlat = lonlat;
//        }
//        public String getCity() {
//            return city;
//        }
//
//        public void setCity(String city) {
//            this.city = city;
//        }
//        public List<Integer> getServices() {
//            return services;
//        }
//
//        public void setServices(List<Integer> services) {
//            this.services = services;
//        }
//
//        public List<Integer> getCartypes() {
//            return cartypes;
//        }
//
//        public void setCartypes(List<Integer> cartypes) {
//            this.cartypes = cartypes;
//        }
//
//        public HashMap<Integer, ArrayList<Prices>> getMenu() {
//            return menu;
//        }
//
//        public void setMenu(HashMap<Integer, ArrayList<Prices>> menu) {
//            this.menu = menu;
//        }
//
//        public boolean is_public() {
//            return is_public;
//        }
//
//        public void setIs_public(boolean is_public) {
//            this.is_public = is_public;
//        }
//
//        public ArrayList<Review> getReviews() {
//            return reviews;
//        }
//
//        public void setReviews(ArrayList<Review> reviews) {
//            this.reviews = reviews;
//        }
//
//        public Integer getRating() {
//            return rating;
//        }
//
//        public void setRating(Integer rating) {
//            this.rating = rating;
//        }
//
//        public boolean is_registered() {
//            return is_registered;
//        }
//
//        public void setIs_registered(boolean is_registered) {
//            this.is_registered = is_registered;
//        }
//
//        public String getStatus() {
//            return status;
//        }
//
//        public void setStatus(String status) {
//            this.status = status;
//        }
//
//        private static class Schedule {
//
//            String comment;
//
//            @SerializedName("mon")
//            ArrayList<Day> monday;
//
//            @SerializedName("tue")
//            ArrayList<Day> tuesday;
//
//            @SerializedName("wed")
//            ArrayList<Day> wednesday;
//
//            @SerializedName("thu")
//            ArrayList<Day> thursday;
//
//            @SerializedName("fri")
//            ArrayList<Day> friday;
//
//            @SerializedName("sat")
//            ArrayList<Day> saturday;
//
//            @SerializedName("sun")
//            ArrayList<Day> sunday;
//
//            public String getComment() {
//                return comment;
//            }
//
//            public void setComment(String comment) {
//                this.comment = comment;
//            }
//
//            public ArrayList<Day> getMonday() {
//                return monday;
//            }
//
//            public void setMonday(ArrayList<Day> monday) {
//                this.monday = monday;
//            }
//
//            public ArrayList<Day> getTuesday() {
//                return tuesday;
//            }
//
//            public void setTuesday(ArrayList<Day> tuesday) {
//                this.tuesday = tuesday;
//            }
//
//            public ArrayList<Day> getWednesday() {
//                return wednesday;
//            }
//
//            public void setWednesday(ArrayList<Day> wednesday) {
//                this.wednesday = wednesday;
//            }
//
//            public ArrayList<Day> getThursday() {
//                return thursday;
//            }
//
//            public void setThursday(ArrayList<Day> thursday) {
//                this.thursday = thursday;
//            }
//
//            public ArrayList<Day> getFriday() {
//                return friday;
//            }
//
//            public void setFriday(ArrayList<Day> friday) {
//                this.friday = friday;
//            }
//
//            public ArrayList<Day> getSaturday() {
//                return saturday;
//            }
//
//            public void setSaturday(ArrayList<Day> saturday) {
//                this.saturday = saturday;
//            }
//
//            public ArrayList<Day> getSunday() {
//                return sunday;
//            }
//
//            public void setSunday(ArrayList<Day> sunday) {
//                this.sunday = sunday;
//            }
//
//            public ArrayList<Day> getToday() {
//                Calendar c = Calendar.getInstance();
//                c.setTime(new Date());
//                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
//
//                switch (dayOfWeek) {
//                    case 2:
//                        return getMonday();
//                    case 3:
//                        return getTuesday();
//                    case 4:
//                        return getWednesday();
//                    case 5:
//                        return getThursday();
//                    case 6:
//                        return getFriday();
//                    case 7:
//                        return getSaturday();
//                    case 1:
//                        return getSunday();
//                    default:
//                        return null;
//                }
//            }
//
//
//            private static class Day {
//                @SerializedName("fr")
//                int from;
//                int to;
//
//                public int getFrom() {
//                    return from;
//                }
//
//                public void setFrom(int from) {
//                    this.from = from;
//                }
//
//                public int getTo() {
//                    return to;
//                }
//
//                public void setTo(int to) {
//                    this.to = to;
//                }
//            }
//
//
//            private static class Contact {
//                public String alias;
//
//                public String comment;
//
//                public String type;
//
//                public String value;
//
//                public String getAlias() {
//                    return alias;
//                }
//
//                public void setAlias(String alias) {
//                    this.alias = alias;
//                }
//
//                public String getComment() {
//                    return comment;
//                }
//
//                public void setComment(String comment) {
//                    this.comment = comment;
//                }
//
//                public String getType() {
//                    return type;
//                }
//
//                public void setType(String type) {
//                    this.type = type;
//                }
//
//                public String getValue() {
//                    return value;
//                }
//
//                public void setValue(String value) {
//                    this.value = value;
//                }
//            }
//        }
//
//        private static class BoxSettings {
//            @SerializedName("booking_type")
//            public BookingType bookingType;
//            public String uid;
//
//            public BookingType getBookingType() {
//                return bookingType;
//            }
//
//            public void setBookingType(BookingType bookingType) {
//                this.bookingType = bookingType;
//            }
//
//            public String getUid() {
//                return uid;
//            }
//
//            public void setUid(String uid) {
//                this.uid = uid;
//            }
//        }
//
//        private static class Review {
//            String author;
//            @SerializedName("author_id")
//            String authorId;
//            String id;
//            Integer mark;
//            String text;
//
//            public String getAuthor() {
//                return author;
//            }
//
//            public void setAuthor(String author) {
//                this.author = author;
//            }
//
//            public String getAuthorId() {
//                return authorId;
//            }
//
//            public void setAuthorId(String authorId) {
//                this.authorId = authorId;
//            }
//
//            public String getId() {
//                return id;
//            }
//
//            public void setId(String id) {
//                this.id = id;
//            }
//
//            public Integer getMark() {
//                return mark;
//            }
//
//            public void setMark(Integer mark) {
//                this.mark = mark;
//            }
//
//            public String getText() {
//                return text;
//            }
//
//            public void setText(String text) {
//                this.text = text;
//            }
//        }
//
//
//
//        private static class Prices {
//            public Double price;
//            public Integer time;
//            public Integer type;
//
//            public Double getPrice() {
//                return price;
//            }
//
//            public void setPrice(Double price) {
//                this.price = price;
//            }
//
//            public Integer getTime() {
//                return time;
//            }
//
//            public void setTime(Integer time) {
//                this.time = time;
//            }
//
//            public Integer getType() {
//                return type;
//            }
//
//            public void setType(Integer type) {
//                this.type = type;
//            }
//        }
//
//        public String serialize() {
//            Gson gson = new Gson();
//            return gson.toJson(this);
//        }
//
//        public static Washer deserialize(String serializedData) {
//            Gson gson = new Gson();
//            return gson.fromJson(serializedData, Washer.class);
//        }
//    }
//
//
//    public String serialize() {
//        Gson gson = new Gson();
//        return gson.toJson(this);
//    }
//
//    public static Washer deserialize(String serializedData) {
//        Gson gson = new Gson();
//        return gson.fromJson(serializedData, Washer.class);
//    }
}
