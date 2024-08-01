<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>File Upload</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.13.0/css/all.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/css/toastr.min.css" rel="stylesheet" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/toastr.js/latest/js/toastr.min.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/index.js"></script>
    <style>
    body {
            margin: 2rem;
        }
        .container {
            margin-top: 2rem;
        }
        .table th, .table td {
            text-align: center;
            vertical-align: middle;
        }
        #progress-img {
            display: block;
            margin: 0 auto;
        }
    </style>
</head>
<body>
<div class="container">
    <h3>File Upload</h3>
    <p>Upload .csv files:</p>
    <form id="fileUploadForm">
        <div class="form-group">
            <input type="file" multiple="multiple" name="files" class="form-control-file" accept=".csv" onchange="loadFiles()"/>
        </div>
    </form>
    <br/>

    
    <form id="uploadForm">
        
        <button type="button" class="btn btn-primary" style="float: right; margin-bottom: 2rem;" onclick="uploadAllFiles()" id="uploadAllBtn"><i class="fa fa-upload"></i> Upload Selected</button>
        <button type="button" class="btn btn-primary" style="float: right; margin-bottom: 2rem; margin-right: -15rem;" onclick="downloadAllFiles()" id="downloadAllBtn"><i class="fa fa-download"></i> Download Selected</button>

    </form>
    <br/>

    <table class="table table-striped table-bordered">
        <thead>
        <tr>
            <th scope="col">Select</th>
            
            <th scope="col">Filename</th>
            <th scope="col">Size</th>
            <th scope="col">Actions</th>
            <th>Progress</th>
            

        </tr>
        </thead>
        <tbody id="fileTable">
        </tbody>
    </table>
</div>

</body>
</html>
