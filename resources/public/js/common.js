function execVerb(verb, url, body) {
  const xhr = new XMLHttpRequest();
  xhr.open(verb, url);
  xhr.setRequestHeader("Content-Type", "application/json");

  xhr.onreadystatechange = function () {
    if (xhr.readyState === 4) {
      console.log(xhr.responseText);
    }};

  xhr.send(body);
}

function addEmptyCategory(category) {
  execVerb('PUT', '/v1/'.concat(category, '/add'));
  save();
}

function removeEmptyCategory(category) {
  execVerb('DELETE', '/v1/'.concat(category, '/delete-empty'));
  save();
}

function incItem(category, item) {
  execVerb('PUT', '/v1/'.concat(category, '/', item, '/add'));
  save();
}

function decItem(category, item) {
  execVerb('DELETE', '/v1/'.concat(category, '/', item, '/remove'));
  save();
}

function save() {
  execVerb('POST', '/v1/save');
  location.reload();
}
