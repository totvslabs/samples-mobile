
Pod::Spec.new do |s|
  s.name         = "CameraCore"
  s.version      = "0.0.1"
  s.summary      = "Core classes for TOTVS camera capabilities"
  s.description  = "TODO: Add long description of the pod here."
  s.homepage      = "https://github.com/totvslabs/samples-mobile"
  s.license       = 'MIT'
  s.author        = { "Jansel Rodriguez" => "jvra16@gmail.com" }
  s.source        = { :git => "https://github.com/totvslabs/samples-mobile.git", :tag => "#{s.version}" }
  s.source_files  = "CameraCore/**/*.{swift,h,m}"
  
  s.requires_arc   = true
  s.platform       = :ios, '9.0'
  s.swift_version  = '5.0'
  
end
