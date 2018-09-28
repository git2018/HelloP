package com.albertsu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class MVPPlugin extends AnAction {
    private Project project;

    private String path = "";
    private String packageName ="";
    private String className = "";
    private String basePath = "cn.albert.xmvp.base";

    private boolean isFragment = false;

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();

        Pair<String,Boolean> pair = Messages.showInputDialogWithCheckBox(
                "请输入类名称",
                "Winsoft MVP Creator",
                "Fragment",
                false,
                true,
                null,
                "",
                null
        );
        className = pair.first;
        isFragment = pair.second;

        VirtualFile selectGroup = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (selectGroup == null) return;
        path = selectGroup.getPath() + "/" + className.toLowerCase();
        packageName = path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");

//        className = Messages.showInputDialog(project, "请输入类名称", "Winsoft MVP Creator", Messages.getQuestionIcon());

        //自动判断Base类
//        if (basePath == null ||basePath.isEmpty()) {
//            //寻找基类位置
//            basePath = traverseFolder(path.substring(0, path.indexOf("java")));
//            //生成基类包名
//            basePath = basePath.substring(basePath.indexOf("java") + 5, basePath.length()).replace("/", ".").replace("\\",".");
//        }

        if (className.equals("")) {
            Messages.showInfoMessage(project, "没有输入类名", "警告");
        }else {
            createClassMvp();
        }
    }

    /**
     * 创建MVP架构
     */
    private void createClassMvp() {
        //首字母大写
        className = className.substring(0, 1).toUpperCase() + className.substring(1);

        if (isFragment) {
            writetoFile(readFile("Fragment.txt"), path, className + "Fragment.java");
        } else {
            writetoFile(readFile("Activity.txt"), path, className + "Activity.java");
        }
        writetoFile(readFile("Contract.txt"), path, className + "Contract.java");
        writetoFile(readFile("Presenter.txt"), path, className + "Presenter.java");

        project.getBaseDir().refresh(false,true);
    }

    private String readFile(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("code/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commonReplace(content);
    }

    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

    private String traverseFolder(String path) {
        File file = new File(path);
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    if (file2.getName().endsWith("base")){
                        return file2.getAbsolutePath();
                    }
                    list.add(file2);
                }
            }
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        if (file2.getName().endsWith("base")){
                            return file2.getAbsolutePath();
                        }
                        list.add(file2);
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
        System.out.println("没有发现文件");
        return "";
    }

    private String commonReplace(String content){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());

        content = content
                .replace("&package&", packageName)
                .replace("&base&", basePath)
                .replace("&Activity&", className + "Activity")
                .replace("&Fragment&", className + "Fragment")
                .replace("&Contract&", className + "Contract")
                .replace("&Presenter&", className + "Presenter")
                .replace("&Time&", simpleDateFormat.format(date));
        return content;
    }
}
