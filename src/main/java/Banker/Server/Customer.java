package Banker.Server;

import java.io.Serializable;

public class Customer implements Serializable {
    private static final long serialVersionUID = -4385080825232129934L;
    private String tz;
    private String name;
    private String address;
    private String city;

    public Customer(String tz,String n,String add,String c)
    {
        this.name=n;
        this.address=add;
        this.city=c;
        this.tz=tz;
    }

    public String getTZ(){return this.tz;}

    public String getName(){return this.name;}

    public String getCity(){return this.city;}

    public String getAddress(){return this.address;}

    public void setName(String n)
    {
        this.name = n;
    }

    public void setAddress(String a, String c)
    {
        this.address = a;
        this.city = c;
    }

    @Override
    public String toString()
    {
        return name + " (" + tz + ") " + address + ", " + city;
    }

}