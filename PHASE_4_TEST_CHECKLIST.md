# File Upload Feature - Quick Test Checklist

## Pre-Test Verification

- [ ] API Gateway Service running on port 9001
  ```powershell
  netstat -ano | findstr :9001
  ```
- [ ] Secure File Service running on port 9090
  ```powershell
  netstat -ano | findstr :9090
  ```
- [ ] Hub Server running on port 7070

  ```powershell
  netstat -ano | findstr :7070
  ```

- [ ] React Dashboard running on port 5173
  - Open: http://localhost:5173

## Test Case 1: File Upload

### Steps:

1. Open React Dashboard (http://localhost:5173)
2. In Service Registry, click "SecureFileService"
3. In File Service panel, click "Choose File"
4. Select a text file from your computer (e.g., test.txt)
5. Click "Upload File" button

### Expected Results:

- [ ] ✅ File name and size appear in upload area
- [ ] ✅ Upload progress spinner appears when button clicked
- [ ] ✅ Success message appears: "✅ [filename] uploaded successfully"
- [ ] ✅ File automatically appears in "Stored Files" list
- [ ] ✅ Upload area clears (file selection removed)
- [ ] ✅ No error messages in red

### Browser Console Check (F12 → Console):

- [ ] No error messages
- [ ] WebSocket messages show successful transfer
- [ ] No "Failed to upload" messages

### Server Logs Check:

Open terminal where API Gateway is running and look for:

- [ ] `[ApiGateway] Uploading file: [filename] ...`
- [ ] `[ApiGateway] Upload response: SUCCESS::...`
- [ ] `[ApiGateway] ✓ File uploaded successfully: [filename]`

## Test Case 2: File List

### Steps:

1. After uploading a file (from Test Case 1)
2. Click "Refresh" button in Stored Files section (or "⟳" on mobile)

### Expected Results:

- [ ] ✅ File list updates
- [ ] ✅ Newly uploaded file appears in the list
- [ ] ✅ File size shown in parentheses (on list retrieval)
- [ ] ✅ No errors displayed

## Test Case 3: File Download

### Steps:

1. In "Stored Files" list, click "Get" (or "↓" on mobile) button next to a file

### Expected Results:

- [ ] ✅ Download progress spinner appears
- [ ] ✅ File downloads to your Downloads folder
- [ ] ✅ Success message appears: "✅ [filename] downloaded successfully"
- [ ] ✅ No error messages

## Test Case 4: Multiple Files

### Steps:

1. Upload 3-5 different text files one by one
2. Click Refresh in File List
3. Download each file

### Expected Results:

- [ ] ✅ All files appear in the list
- [ ] ✅ All files download correctly
- [ ] ✅ No errors with multiple operations

## Test Case 5: Error Handling

### Test 5a: Try without selecting file

1. Click "Upload File" without selecting a file

### Expected Result:

- [ ] ✅ Error message: "Please select a file to upload"

### Test 5b: Try with Secure File Service offline

1. Stop the Secure File Service (kill java process running on 9090)
2. Try to upload a file

### Expected Result:

- [ ] ✅ Error message appears
- [ ] ✅ No crash in browser or API Gateway

## Responsive Design Check

### Mobile (320px width):

- [ ] ✅ "Choose File" button visible
- [ ] ✅ Upload area readable
- [ ] ✅ File list scrollable if multiple files
- [ ] ✅ "Get" button works on mobile-sized screen

### Tablet (768px width):

- [ ] ✅ All buttons visible
- [ ] ✅ Layout readable
- [ ] ✅ File list scrollable

### Desktop (1024px+ width):

- [ ] ✅ All sections properly spaced
- [ ] ✅ Full text labels visible (not abbreviated)

## Performance Checks

- [ ] Upload completes in < 5 seconds
- [ ] File list refresh completes in < 2 seconds
- [ ] Download completes in < 3 seconds
- [ ] No lag or freezing in UI

## Summary

- **Total Tests:** 5 main + 5 sub-tests = 10+ assertions
- **Pass Criteria:** All checkmarks should be filled
- **Issues Found:** Document in separate "ISSUES_FOUND.md" file

---

**Date Tested:** ******\_\_\_******  
**Tester:** ******\_\_\_******  
**Result:** ✅ PASS / ❌ FAIL / 🟡 PARTIAL
