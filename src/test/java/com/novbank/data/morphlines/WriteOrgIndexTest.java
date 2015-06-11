package com.novbank.data.morphlines;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.apache.commons.io.Charsets;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by hp on 2015/6/11.
 */
public class WriteOrgIndexTest {
    @Test
    public void testWriteOrgIndex(){
        File root = new File("D:\\Workspace\\data\\全球价值链\\gvc\\site\\temp\\org");
        Map<String,List<String>> orgs = Maps.newLinkedHashMap();
        for(File child : root.listFiles()){
            if(child.isDirectory()){
                orgs.put(child.getName(), Lists.<String>newArrayList());
                for(File file:child.listFiles()){
                    if(file.getName().equalsIgnoreCase("index.html")) continue;
                    if(file.isFile())
                        orgs.get(child.getName()).add(file.getName());
                }
            }
        }
        String temp ="<div class=\"list-group\">\n";
        for(String orgName : orgs.keySet()){
            temp += "<a href=\"../"+orgName+"/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">"+ orgs.get(orgName).size()+"</span>\n" +
                    "    "+orgName+"\n" +
                    "  </a>";

            String html = "\n" +
                    "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "        <meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\" />\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                    "    <link rel=\"stylesheet\" href=\"../../assets/cosmo/bootstrap.css\" media=\"screen\">\n" +
                    "    <link rel=\"stylesheet\" href=\"../../assets/assets/css/bootswatch.min.css\">\n" +
                    "    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->\n" +
                    "    <!--[if lt IE 9]>\n" +
                    "    <script src=\"../../assets/bower_components/html5shiv/dist/html5shiv.js\"></script>\n" +
                    "    <script src=\"../../assets/bower_components/respond/dest/respond.min.js\"></script>\n" +
                    "    <![endif]-->\n" +
                    "    <style>\n" +
                    "        body{\n" +
                    "            font-family:\"Helvetica Neue\",Helvetica,Arial,\"Hiragino Sans GB\",\"Hiragino Sans GB W3\",\"Microsoft YaHei UI\",\"Microsoft YaHei\",\"WenQuanYi Micro Hei\",sans-serif\n" +
                    "        }\n" +
                    "\n" +
                    "        h1,.h1,h2,.h2,h3,.h3,h4,.h4,.lead{\n" +
                    "            font-family:\"Helvetica Neue\",Helvetica,Arial,\"Hiragino Sans GB\",\"Hiragino Sans GB W3\",\"Microsoft YaHei UI\",\"Microsoft YaHei\",\"WenQuanYi Micro Hei\",sans-serif\n" +
                    "        }\n" +
                    "    </style>    <title>Global Value Chains：Challenges，opportunities，and Implications for policy</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<!-- menu -->\n" +
                    "<div class=\"navbar navbar-default navbar-fixed-top\">\n" +
                    "    <div class=\"container\">\n" +
                    "        <div class=\"navbar-header\">\n" +
                    "            <a href=\"/\" class=\"navbar-brand\"><b><i class=\"glyphicon glyphicon-globe\"></i>&nbsp;GVC&nbsp;&nbsp;</b></a>\n" +
                    "            <button class=\"navbar-toggle\" type=\"button\" data-toggle=\"collapse\" data-target=\"#navbar-main\">\n" +
                    "                <span class=\"icon-bar\"></span>\n" +
                    "                <span class=\"icon-bar\"></span>\n" +
                    "                <span class=\"icon-bar\"></span>\n" +
                    "            </button>\n" +
                    "        </div>\n" +
                    "        <div class=\"navbar-collapse collapse\" id=\"navbar-main\">\n" +
                    "            <ul class=\"nav navbar-nav\">\n" +
                    "                <li class=\"dropdown\">\n" +
                    "                    <a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\" id=\"research\">简报 <span class=\"caret\"></span></a>\n" +
                    "                    <ul class=\"dropdown-menu\" aria-labelledby=\"research\">\n" +
                    "                        <li><a href=\"#\">报告目录</a></li>\n" +
                    "                        <li class=\"divider\"></li>\n" +
                    "\t\t\t<li><a href=\"#\">2015年第1期</a></li>\n" +
                    "\t\t\t<li><a href=\"#\">2015年第2期</a></li>                    \n" +
                    "                    </ul>\n" +
                    "                </li>\n" +
                    "                <li>\n" +
                    "                    <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\" id=\"orginazation\">研究机构 <span class=\"caret\"></span></a>\n" +
                    "                    <ul class=\"dropdown-menu\" aria-labelledby=\"orginazation\">\n" +
                    "                        <li><a href=\"/agent/OECD\">OECD</a></li>\n" +
                    "                        <li><a href=\"/agent/WTO\">WTO</a></li>\n" +
                    "                        <li><a href=\"/agent/ITC\">ITC</a></li>\n" +
                    "                        <li><a href=\"/agent/UNCTAD\">UNCTAD</a></li>\n" +
                    "                        <li class=\"divider\"></li>\n" +
                    "                        <li><a href=\"/agent/Apec\">Apec</a></li>\n" +
                    "                        <li class=\"divider\"></li>\n" +
                    "                        <li><a href=\"/agent/Duke CGGC\">Duke CGGC</a></li>\n" +
                    "                    </ul>\n" +
                    "                </li>\n" +
                    "                <li>\n" +
                    "                    <a href=\"#\">相关会议</a>\n" +
                    "                </li>\n" +
                    "                <li>\n" +
                    "                    <a href=\"#\">关于我们</a>\n" +
                    "                </li>\n" +
                    "            </ul>\n" +
                    "            <form class=\"navbar-form navbar-left\" role=\"search\" action=\"/search\" method=\"post\">\n" +
                    "                <div class=\"form-group\">\n" +
                    "                    <input class=\"form-control\" placeholder=\"输入搜索条件\" type=\"text\" id=\"keyword\" name=\"keyword\">\n" +
                    "                </div>\n" +
                    "                <button type=\"submit\" class=\"btn btn-default\"><i class=\"glyphicon glyphicon-search icon-white\"></i></button>\n" +
                    "            </form>\n" +
                    "            <ul class=\"nav navbar-nav navbar-right\">\n" +
                    "                <li><a href=\"#\">登录</a></li>\n" +
                    "            </ul>\n" +
                    "\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</div>\n" +
                    "<!-- menu --><div class=\"row\">\n" +
                    "    <div class=\"col-lg-4 col-md-4 col-sm-12\">\n" +
                    "        <div class=\"list-group\">\n" +
                    "    <a href=\"../AfDB/index.html\" class=\"list-group-item\">\n" +
                    "        <span class=\"badge\">1</span>\n" +
                    "        AfDB\n" +
                    "    </a><a href=\"../ANSI/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    ANSI\n" +
                    "</a><a href=\"../CACI International Inc/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    CACI International Inc\n" +
                    "</a><a href=\"../CRS/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    CRS\n" +
                    "</a><a href=\"../DHS/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    DHS\n" +
                    "</a><a href=\"../Duke CGGC/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">25</span>\n" +
                    "    Duke CGGC\n" +
                    "</a><a href=\"../ECFR/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    ECFR\n" +
                    "</a><a href=\"../GAO/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    GAO\n" +
                    "</a><a href=\"../HSPI/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    HSPI\n" +
                    "</a><a href=\"../IBM/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    IBM\n" +
                    "</a><a href=\"../NIST/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    NIST\n" +
                    "</a><a href=\"../ODI/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    ODI\n" +
                    "</a><a href=\"../OECD/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">10</span>\n" +
                    "    OECD\n" +
                    "</a><a href=\"../SAIC/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    SAIC\n" +
                    "</a><a href=\"../TGVCI/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">8</span>\n" +
                    "    TGVCI\n" +
                    "</a><a href=\"../The World Economy/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    The World Economy\n" +
                    "</a><a href=\"../TIA/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    TIA\n" +
                    "</a><a href=\"../WB/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    WB\n" +
                    "</a><a href=\"../WH/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">2</span>\n" +
                    "    WH\n" +
                    "</a><a href=\"../世界经济论坛/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">2</span>\n" +
                    "    世界经济论坛\n" +
                    "</a><a href=\"../卡内基/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    卡内基\n" +
                    "</a><a href=\"../卡内基国际和平基金会/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">2</span>\n" +
                    "    卡内基国际和平基金会\n" +
                    "</a><a href=\"../埃森哲/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">3</span>\n" +
                    "    埃森哲\n" +
                    "</a><a href=\"../对外关系委员会/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">2</span>\n" +
                    "    对外关系委员会\n" +
                    "</a><a href=\"../布鲁金斯学会/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    布鲁金斯学会\n" +
                    "</a><a href=\"../彼得森国际经济研究院/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">2</span>\n" +
                    "    彼得森国际经济研究院\n" +
                    "</a><a href=\"../战略与国际问题研究中心/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    战略与国际问题研究中心\n" +
                    "</a><a href=\"../波士顿咨询/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    波士顿咨询\n" +
                    "</a><a href=\"../经纶国际经济研究院/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">2</span>\n" +
                    "    经纶国际经济研究院\n" +
                    "</a><a href=\"../罗兰贝格/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">1</span>\n" +
                    "    罗兰贝格\n" +
                    "</a><a href=\"../麦肯锡/index.html\" class=\"list-group-item\">\n" +
                    "    <span class=\"badge\">3</span>\n" +
                    "    麦肯锡\n" +
                    "</a></div>    </div>\n" +
                    "    <div class=\"col-lg-8 col-md-8 col-sm-12\">\n" +
                    "        <h1>"+orgName+"<h2>\n<div class =\"list-group\">";
            for(String fileName:orgs.get(orgName)){
                System.out.println(orgName);
                System.out.println(fileName);
                html+="<p><a class= \"list-group-item\" href=\""+fileName+"\">"+fileName.substring(0,fileName.length()-4)+"</a></p>";
            }
            html+="    </div></div>\n" +
                    "</div>\n" +
                    "\n" +
                    "<footer>\n" +
                    "\n" +
                    "</footer>\n" +
                    "<script src=\"../../assets/bower_components/jquery/dist/jquery.min.js\"></script>\n" +
                    "<script src=\"../../assets/bower_components/bootstrap/dist/js/bootstrap.min.js\"></script>\n" +
                    "<script src=\"../../assets/assets/js/bootswatch.js\"></script></body>\n" +
                    "</html>";
            try {
                Files.write(html, new File(root, orgName + "/index.html"), Charsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        temp+="</div>";
        System.out.println(temp);

    }
}
