var filesToDownload = [];
let loadedFiles = [];



function loadExistingFiles() {

    fetch('/getFiles', { method: 'GET' })
        .then(response => response.json())
        .then(data => {
            if (data.length > 0) {
                const uploadButton = document.getElementById('uploadAllBtn');
                const downloadButton = document.getElementById('downloadAllBtn');
    downloadButton.style.visibility = "visible";
    uploadButton.style.visibility = "hidden";
                data.forEach(file => {
                    filesToDownload.push(file);
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td><input type="checkbox" class="file-checkbox" value="${file.id}"></td>
                        <td class="download-label">${file.name}</td>
                        <td>${file.size}</td>
                        <td><button type="button" class="btn btn-success download-btn"><i class="fa fa-download"></i> Download</button> <button type="button" class="btn btn-danger delete-btn"><i class="fa fa-trash"></i> Delete</button></td>
                        <td><progress class="download-progress" value="0" max="100"></progress></td>
                    `;
                    document.getElementById('fileTable').appendChild(row);
                });

                const deleteButtons = document.querySelectorAll('.delete-btn');
                deleteButtons.forEach((button, index) => {
                    button.addEventListener('click', function() {
                        button.disabled = true;
                        deleteFile(filesToDownload[index]);
                    });
                });

                const downloadButtons = document.querySelectorAll('.download-btn');
                downloadButtons.forEach((button, index) => {
                    button.addEventListener('click', function() {
                        button.disabled = true;  
                        downloadFileInChunks(filesToDownload[index], button.closest('tr').querySelector('.download-progress'), button);
                    });
                });
            }
        });
}

function deleteFile(file) {
    fetch('/deleteFile?filename=' + encodeURIComponent(file.name), {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 200) {
            toastr.success('File deleted successfully!');
            const allRows = Array.from(document.querySelectorAll('tr'));
            const row = allRows.find(row => row.querySelector('.download-label')?.innerText === file.name);
            if (row) {
                row.remove();
            } else {
                console.warn('No matching row found for file:', file.name);
            }
        } else {
            console.error('Error deleting file:', response);
        }
    }).catch(error => {
        console.error('Error deleting file:', error);
    });
}
function downloadFileInChunks(file, progressElement, button) {
    if (!file) {
        console.error('File path is undefined');
        return;
    }

    const chunkSize = 1024 * 1024 * 200; // 200 MB
    let chunks = [];
    let chunkIndex = 1;
    let totalChunks = 0; 
    let filename = file.name;

    function fetchChunk() {
        fetch(`/downloadFileChunk?filename=${encodeURIComponent(filename)}&chunkIndex=${chunkIndex}`, {
            method: 'GET'
        }).then(response => {
            if (response.status === 204) {
                mergeChunks();
            } else if (!response.ok) {
                throw new Error('Network response was not ok');
            } else {
                return response.arrayBuffer();
            }
        }).then(arrayBuffer => {
            if (arrayBuffer != null && arrayBuffer.byteLength > 0) {
                chunks.push(new Uint8Array(arrayBuffer));
                chunkIndex++;
                updateProgress();
                fetchChunk();
            } 
        }).catch(error => {
            console.error('There was a problem with the fetch operation:', error);
            button.disabled = false;  
        });
    }

    function updateProgress() {
        const progress = Math.min((chunkIndex / totalChunks) * 100, 100);
        progressElement.value = isFinite(progress) ? progress : 0;
    }

    function mergeChunks() {
        if (chunks.length === 0) {
            console.error('No chunks downloaded');
            button.disabled = false; 
            return;
        }

        const combinedArray = new Uint8Array(chunks.reduce((acc, chunk) => acc + chunk.length, 0));
        let offset = 0;
        chunks.forEach(chunk => {
            combinedArray.set(chunk, offset);
            offset += chunk.length;
        });

        const blob = new Blob([combinedArray], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        button.disabled = false;  
    }

    fetch('/splitFile?filename=' + encodeURIComponent(filename), {
        method: 'POST'
    }).then(response => {
        if (response.status === 200) {
            totalChunks = parseInt(response.headers.get('X-Total-Chunks'), 10) || 0;
            fetchChunk();
        } else {
            button.disabled = false;  
        }
    }).catch(error => {
        console.error('There was a problem with the split file operation:', error);
        button.disabled = false;  
    });
}

function uploadFile(index) {
    const file = loadedFiles[index];
    if (!file) {
        console.error('Invalid file index:', index);
        return;
    }

    const chunkSize = 1024 * 1024 * 200; // 200 MB
    const totalChunks = Math.ceil(file.size / chunkSize);

    function uploadChunk(chunkIndex = 0) {
        const start = chunkIndex * chunkSize;
        const end = Math.min(start + chunkSize, file.size);
        const chunk = file.slice(start, end);

        const formData = new FormData();
        formData.append('file', chunk);
        formData.append('chunkIndex', chunkIndex);
        formData.append('totalChunks', totalChunks);
        formData.append('fileName', file.name);

        fetch('/uploadFileChunk', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(data => {
                console.log(data);
                updateProgress(chunkIndex);
                if (chunkIndex < totalChunks - 1) {
                    uploadChunk(chunkIndex + 1);
                } else {
                    toastr.success('File uploaded successfully!');
                }
            })
            .catch(error => console.error('Error uploading chunk', chunkIndex, error));
    }

    function updateProgress(chunkIndex) {
        const progress = Math.min(((chunkIndex + 1) / totalChunks) * 100, 100);
        const progressElement = document.querySelector(`.upload-progress[data-index="${index}"]`);
        if (progressElement) {
            progressElement.value = isFinite(progress) ? progress : 0;
        }
    }

    uploadChunk();
}

function uploadAllFiles() {
    const checkboxes = document.querySelectorAll('.file-checkbox');
    checkboxes.forEach((checkbox, index) => {
        if (checkbox.checked) {
            uploadFile(index);
        }
    });
}

function downloadAllFiles() {
    const checkboxes = document.querySelectorAll('.file-checkbox');
    checkboxes.forEach((checkbox, index) => {
        if (checkbox.checked) {
            downloadFileInChunks(filesToDownload[index], checkbox.closest('tr').querySelector('.download-progress'), checkbox.closest('tr').querySelector('.download-btn'));
        }
    });
}

function loadFiles() {
    const uploadButton = document.getElementById('uploadAllBtn');
const downloadButton = document.getElementById('downloadAllBtn');
    downloadButton.style.visibility = "hidden";
    uploadButton.style.visibility = "visible";
    const fileInput = document.querySelector('input[name="files"]');
    loadedFiles = Array.from(fileInput.files);

    const tableBody = document.getElementById('fileTable');
    tableBody.innerHTML = '';

    loadedFiles.forEach((file, index) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><input type="checkbox" class="file-checkbox"></td>
            <td class="upload-label"></td>
            <td><button type="button" class="btn btn-primary upload-btn"><i class="fa fa-upload"></i> Upload</button></td>
            <td><progress class="upload-progress" value="0" max="100" data-index="${index}"></progress></td>
        `;
        tableBody.appendChild(row);
    });

    const uploadButtons = document.querySelectorAll('.upload-btn');
    const uploadLabels = document.querySelectorAll('.upload-label');
    uploadButtons.forEach((button, index) => {
        button.addEventListener('click', function() {
            uploadFile(index);
        });
    });

    uploadLabels.forEach((label, index) => {
        label.innerHTML = loadedFiles[index].name;
    });
}

function periodicCheck() {
    setInterval(() => {
        const downloadCheckboxes = document.querySelectorAll('.file-checkbox:checked');
        downloadCheckboxes.forEach((checkbox, index) => {
            downloadFileInChunks(filesToDownload[index], checkbox.closest('tr').querySelector('.download-progress'), checkbox.closest('tr').querySelector('.download-btn'));
        });

        uploadAllFiles();
    }, 5000);
}

loadExistingFiles();
loadFiles();
periodicCheck();
