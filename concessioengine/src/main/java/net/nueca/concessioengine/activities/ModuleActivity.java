package net.nueca.concessioengine.activities;

import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.util.Log;
import android.widget.SearchView;

import net.nueca.concessioengine.views.SearchViewEx;
import net.nueca.imonggosdk.activities.ImonggoAppCompatActivity;
import net.nueca.imonggosdk.objects.ProductTag;
import net.nueca.imonggosdk.tools.StringUtilsEx;

import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhymart on 6/3/15.
 * imonggosdk (c)2015
<<<<<<< HEAD
 * /usr/local/heroku/bin:/opt/local/bin:/opt/local/sbin:/Users/rhymart/yes/google-cloud-sdk/bin:/usr/local/apache-maven/apache-maven-3.2.1//bin:/usr/local/mysql/bin:/usr/local/heroku/bin:/opt/local/bin:/opt/local/sbin:/Users/rhymart/yes/google-cloud-sdk/bin:/usr/local/apache-maven/apache-maven-3.2.1//bin:/usr/local/mysql/bin:/usr/local/heroku/bin:/opt/local/bin:/opt/local/sbin:/Users/rhymart/yes/google-cloud-sdk/bin:/usr/local/apache-maven/apache-maven-3.2.1//bin:/usr/local/mysql/bin:/usr/local/heroku/bin:/opt/local/bin:/opt/local/sbin:/Users/rhymart/yes/google-cloud-sdk/bin:/usr/local/apache-maven/apache-maven-3.2.1//bin:/usr/local/mysql/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/git/bin:/Users/rhymart/gradle-all//bin:/Users/rhymart/gradle-1.11//bin:/Users/rhymart/gradle-1.11//bin:/Users/rhymart/gradle-all/bin:/Users/rhymart/gradle-all/bin
 *
 *
 * /usr/local/heroku/bin:/opt/local/bin:/opt/local/sbin:/Users/rhymart/yes/google-cloud-sdk/bin:/usr/local/apache-maven/apache-maven-3.2.1/bin:/usr/local/mysql/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/git/bin:/Users/rhymart/gradle-all/bin
=======
>>>>>>> remotes/origin/gama
 */
public abstract class ModuleActivity extends ImonggoAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public List<String> getProductCategories(boolean includeAll) {
        List<String> categories = new ArrayList<>();

        try {
            List<ProductTag> productTags = getHelper().getProductTags().queryBuilder().distinct().selectColumns("tag").orderByRaw("tag COLLATE NOCASE ASC").where().like("tag", "#%").query();
            for(ProductTag productTag : productTags) {
                if(productTag.getTag().matches("^#[\\w\\-\\'\\+ ]*")) {
                    String category = StringUtilsEx.ucwords(productTag.getTag().replace("#", ""));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(includeAll)
            categories.add(0, "All");

        return categories;
    }

    protected void closeSearchField(SearchViewEx searchView) {

        SearchViewCompat.setQuery(searchView, "", false);
        SearchViewCompat.setIconified(searchView, true);
    }

}
