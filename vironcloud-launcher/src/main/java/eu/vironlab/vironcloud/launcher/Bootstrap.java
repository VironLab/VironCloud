/**
 *   Copyright © 2020 | vironlab.eu | All Rights Reserved.<p>
 * <p>
 *      ___    _______                        ______         ______  <p>
 *      __ |  / /___(_)______________ _______ ___  / ______ ____  /_ <p>
 *      __ | / / __  / __  ___/_  __ \__  __ \__  /  _  __ `/__  __ \<p>
 *      __ |/ /  _  /  _  /    / /_/ /_  / / /_  /___/ /_/ / _  /_/ /<p>
 *      _____/   /_/   /_/     \____/ /_/ /_/ /_____/\__,_/  /_.___/ <p>
 *<p>
 *    ____  _______     _______ _     ___  ____  __  __ _____ _   _ _____ <p>
 *   |  _ \| ____\ \   / / ____| |   / _ \|  _ \|  \/  | ____| \ | |_   _|<p>
 *   | | | |  _|  \ \ / /|  _| | |  | | | | |_) | |\/| |  _| |  \| | | |  <p>
 *   | |_| | |___  \ V / | |___| |__| |_| |  __/| |  | | |___| |\  | | |  <p>
 *   |____/|_____|  \_/  |_____|_____\___/|_|   |_|  |_|_____|_| \_| |_|  <p>
 *<p>
 *<p>
 *   This program is free software: you can redistribute it and/or modify<p>
 *   it under the terms of the GNU General Public License as published by<p>
 *   the Free Software Foundation, either version 3 of the License, or<p>
 *   (at your option) any later version.<p>
 *<p>
 *   This program is distributed in the hope that it will be useful,<p>
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of<p>
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<p>
 *   GNU General Public License for more details.<p>
 *<p>
 *   You should have received a copy of the GNU General Public License<p>
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.<p>
 *<p>
 *   Creation: Dienstag 08 Juni 2021 21:29:09<p>
 *<p>
 *   Contact:<p>
 *<p>
 *     Discordserver:   https://discord.gg/wvcX92VyEH<p>
 *     Website:         https://vironlab.eu/ <p>
 *     Mail:            contact@vironlab.eu<p>
 *<p>
 */

package eu.vironlab.vironcloud.launcher;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {

    private static List<String> repos;
    private static File libDir;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting VironCloud... ");
        libDir = new File(".libs");
        if (!libDir.exists()) {
            Files.createDirectories(libDir.toPath());
        }
        List<URL> launchUrls = new ArrayList<URL>();

        //Copy Cloud
        File vironcloud = new File("vironcloud.jar");
        if (!vironcloud.exists()) {
            Files.copy(Bootstrap.class.getResourceAsStream("/vironcloud.jar"), vironcloud.toPath());
        }
        launchUrls.add(vironcloud.toURI().toURL());


        //Init Props and download Dependencies
        SerializedProperties props = new Gson().fromJson(new InputStreamReader(Bootstrap.class.getResourceAsStream("/launch-info.json")), SerializedProperties.class);
        repos = props.getRepositories();
        for (String depend : props.getDependencies()) {
            System.out.println("Downloading " + depend + "...");
            launchUrls.add(load(depend));
        }

        //Copy Modules
        System.out.println("Copy Modules.... ");
        String extensionDirName = System.getProperty("extensionDir") != null ? System.getProperty("extensionDir") : "extensions";
        File extensionDir = new File(extensionDirName);
        if (!extensionDir.exists()) {
            Files.createDirectories(extensionDir.toPath());
        }
        for (String extension : props.getModules()) {
            System.out.println("Copy Module: " + extension);
            File extensionFile = new File(extensionDir, extension + ".jar");
            if (!extensionFile.exists()) {
                Files.copy(Bootstrap.class.getResourceAsStream("/" + extension + ".jar"), extensionFile.toPath());
            }
        }

        //Start Cloud
        LauncherClassLoader loader = new LauncherClassLoader(launchUrls.toArray(new URL[]{}));
        try {
            final Class<?> main = loader.loadClass("eu.vironlab.vironcloud.VironCloud");
            final Method startMethod = main.getMethod("start", String[].class, String.class, File.class, URLClassLoader.class);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startMethod.invoke(null, args, props.getVersion(), extensionDir, loader);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setContextClassLoader(loader);
            thread.start();
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static URL load(String dependencyStr) throws MalformedURLException {
        String[] splittet = dependencyStr.split(":");
        Dependency dependency = new Dependency(splittet[0], splittet[1], splittet[2]);
        String filePath = dependency.group.replace(".", "/") + "/" + dependency.name + "/" + dependency.version;
        String fileName = dependency.name + "-" + dependency.version + ".jar";
        File folder = new File(libDir, filePath);
        File dest = new File(folder, fileName);
        if (!dest.exists() || fileName.toLowerCase().contains("snapshot")) {
            try {
                if (!folder.exists()) {
                    Files.createDirectories(folder.toPath());
                }
                URL server = new URL("https://repo1.maven.org/maven2/");
                for (String repo : repos) {
                    URL url = new URL(repo + filePath + "/" + fileName);
                    if (is200(url)) {
                        server = url;
                    }
                }
                InputStream stream = server.openStream();
                Files.copy(stream, dest.toPath());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dest.toURI().toURL();
    }

    public static boolean is200(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        conn.connect();
        int rs = conn.getResponseCode();
        conn.disconnect();
        return rs == 200;
    }

    public static class Dependency {
        public String group;
        public String name;
        public String version;

        public Dependency(String group, String name, String version) {
            this.group = group;
            this.name = name;
            this.version = version;
        }
    }

    public static class LauncherClassLoader extends URLClassLoader {

        public LauncherClassLoader(URL[] urls) {
            super(urls, getSystemClassLoader());
        }

        static {
            ClassLoader.registerAsParallelCapable();
        }

        @Override
        protected void addURL(URL url) {
            super.addURL(url);
        }
    }

}
